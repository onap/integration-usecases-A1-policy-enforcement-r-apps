/*
 * Copyright (C) 2021 Samsung Electronics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */


package org.onap.rapp.datacollector.service;

import static java.util.Objects.nonNull;
import static org.onap.rapp.datacollector.service.PMService.CELL_FIELD_NAME;
import static org.onap.rapp.datacollector.service.PMService.VALUE_NAME;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.onap.rapp.datacollector.entity.fileready.FileReadyEvent;
import org.onap.rapp.datacollector.entity.fileready.MeasDataCollection;
import org.onap.rapp.datacollector.entity.fileready.MeasDataCollection.MeasInfo;
import org.onap.rapp.datacollector.entity.fileready.MeasDataCollection.MeasInfo.MeasValue;
import org.onap.rapp.datacollector.entity.ves.AdditionalMeasurements;
import org.onap.rapp.datacollector.entity.ves.CommonEventHeader;
import org.onap.rapp.datacollector.entity.ves.Event;
import org.onap.rapp.datacollector.entity.ves.MeasurementFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

@Service
public class FileReadyParserImpl extends ParserAbstractClass implements VesParser {

    private static final Logger logger = LoggerFactory.getLogger(FileReadyParserImpl.class);

    private static class FileReadyEventDeserializer implements JsonDeserializer<FileReadyEvent> {

        @Override
        public FileReadyEvent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {
            Optional<JsonObject> eventJsonObject = getEventJsonObject(jsonElement);
            CommonEventHeader header = getHeaderJsonObject(eventJsonObject.orElse(null), jsonDeserializationContext);
            header.setStartEpochMicrosec(header.getStartEpochMicrosec() * 1000);
            header.setLastEpochMicrosec(header.getLastEpochMicrosec() * 1000);

            Optional<JsonObject> measDataCollectionJson = getMeasDataCollectionJson(eventJsonObject.orElse(null));
            if (measDataCollectionJson.isPresent()) {
                MeasDataCollection measDataCollection = jsonDeserializationContext.deserialize(measDataCollectionJson.get(), MeasDataCollection.class);
                logger.trace("measDataCollection {}", measDataCollection);
                return FileReadyEvent.builder().commonEventHeader(header).measDataCollection(measDataCollection).build();
            } else {
                logger.error("MeasDataCollection was not found {}", eventJsonObject);
                throw new JsonParseException("MeasDataCollection was not found");
            }
        }

        private Optional<JsonObject> getMeasDataCollectionJson(JsonObject obj) {
            if (nonNull(obj)) {
                Optional<JsonObject> fileReadyJson = Optional.ofNullable(obj.getAsJsonObject(FILE_READY_EVENT_UNIQUE_ELEMENT));
                if (fileReadyJson.isPresent()) {
                    return Optional.ofNullable(fileReadyJson.get().getAsJsonObject("measDataCollection"));
                }
            }
            return Optional.empty();
        }
    }

    private List<Event> convertFileReadyEventToEventList(FileReadyEvent fileReadyEvent, String eventString) {
        List<Event> events = new ArrayList<>();
        long averageMeasInterval = getAverageMeasInterval(fileReadyEvent);
        fileReadyEvent.getMeasDataCollection().getMeasInfoList()
                .forEach(measInfo -> measInfo.getMeasValuesList().stream()
                        .filter(measValue -> hasListOfTypesSameSizeAsListOfResults(measInfo, measValue))
                        .forEach(measValue -> events.add(createEvent(fileReadyEvent, measInfo, measValue, eventString, averageMeasInterval))));
        return events;
    }

    private Event createEvent(FileReadyEvent fileReadyEvent, MeasInfo measInfo, MeasValue measValue, String eventString, long averageMeasInterval) {
        List<AdditionalMeasurements> additionalMeasList = new ArrayList<>();
        measValue.getMeasResults()
                .forEach(measResult -> {
                            Map<String, String> hashMap = new HashMap<>();
                            hashMap.put(VALUE_NAME, measResult.getSValue());
                            additionalMeasList.add(AdditionalMeasurements.builder()
                                    .withName(measInfo.getMeasTypes().getSMeasTypesList().get(measResult.getP() - 1))
                                    .withHashMap(hashMap).build());
                        }
                );
        additionalMeasList.add(AdditionalMeasurements.builder()
                .withName(CELL_FIELD_NAME)
                .withHashMap(Collections.singletonMap(CELL_FIELD_NAME, measValue.getMeasObjInstId())).build());

        MeasurementFields measurementFields = MeasurementFields.builder()
                .measurementInterval(averageMeasInterval)
                .additionalMeasurements(additionalMeasList)
                .build();
        Event createdEvent = Event.of(createEventHeader(fileReadyEvent, averageMeasInterval), measurementFields);
        createdEvent.raw = eventString;
        return createdEvent;
    }

    private CommonEventHeader createEventHeader(FileReadyEvent fileReadyEvent, long averageMeasInterval) {
        CommonEventHeader headerCopy = gson.fromJson(gson.toJson(fileReadyEvent.getCommonEventHeader()), CommonEventHeader.class);
        headerCopy.setLastEpochMicrosec(headerCopy.getStartEpochMicrosec() + averageMeasInterval);
        fileReadyEvent.getCommonEventHeader().setStartEpochMicrosec(headerCopy.getLastEpochMicrosec());
        return headerCopy;
    }

    private boolean hasListOfTypesSameSizeAsListOfResults(MeasInfo measInfo, MeasValue measValue) {
        return measInfo.getMeasTypes().getSMeasTypesList().size() == measValue.getMeasResults().size();
    }

    private long getAverageMeasInterval(FileReadyEvent fileReadyEvent) {
        int noOfMeasurment = fileReadyEvent.getMeasDataCollection().getMeasInfoList().size();
        long difference = fileReadyEvent.getCommonEventHeader().getLastEpochMicrosec() - fileReadyEvent.getCommonEventHeader().getStartEpochMicrosec();
        return noOfMeasurment == 0 ? 0 : difference / noOfMeasurment;
    }

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(FileReadyEvent.class, new FileReadyEventDeserializer()).create();

    @Override
    public List<Event> parse(final String eventString) {
        logger.debug("parsing ves event {}", eventString);
        FileReadyEvent fileReadyEvent = gson.fromJson(eventString, FileReadyEvent.class);

        return convertFileReadyEventToEventList(fileReadyEvent, eventString);
    }
}
