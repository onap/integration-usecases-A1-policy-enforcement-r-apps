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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

@Service
public class FileReadyParserImpl extends ParserAbstractClass implements VesParser {

    private static final Logger logger = LoggerFactory.getLogger(FileReadyParserImpl.class);

    public static final String MAP_ENTITY_DELIMITER = ",";
    public static final String MAP_VALUES_DELIMITER = ":";

    /**
     * Parse incoming Json string into list of Events
     *
     * @param eventString json from PM Mapper
     * @return list of events
     */
    @Override
    public List<Event> parse(final String eventString) {
        logger.debug("parsing ves event {}", eventString);
        FileReadyEvent fileReadyEvent = gson.fromJson(eventString, FileReadyEvent.class);
        return convertFileReadyEventToEventList(fileReadyEvent, eventString);
    }

    /**
     * Convert FileReadyEvent event into list of events which will be stored in database
     *
     * @param fileReadyEvent object created from PM Mapper response
     * @param eventString Json event in string
     * @return list of events
     */
    private List<Event> convertFileReadyEventToEventList(FileReadyEvent fileReadyEvent, String eventString) {
        List<Event> events = new ArrayList<>();
        long averageMeasInterval = getAverageMeasInterval(fileReadyEvent);
        fileReadyEvent.getMeasDataCollection().getMeasInfoList()
                .forEach(measInfo -> measInfo.getMeasValuesList().stream()
                        .filter(measValue -> hasListOfTypesSameSizeAsListOfResults(measInfo, measValue))
                        .forEach(measValue -> events.add(createEvent(fileReadyEvent, measInfo, measValue, eventString, averageMeasInterval))));
        return events;
    }

    /**
     * Creates individual event from FileReadyEvent data
     *
     * @param fileReadyEvent bject created from PM Mapper response
     * @param measInfo measurement Info object
     * @param measValue measurement Value object
     * @param eventString Json event in string
     * @param averageMeasInterval calculated average interval
     * @return Event object
     */
    private Event createEvent(FileReadyEvent fileReadyEvent, MeasInfo measInfo, MeasValue measValue, String eventString, long averageMeasInterval) {
        List<AdditionalMeasurements> additionalMeasList = new ArrayList<>();
        // Adding measurement's results to additionalMeasList
        measValue.getMeasResults()
                .forEach(measResult -> {
                            Map<String, String> hashMap = createAdditionalMeasurementHashMap(measResult.getSValue());
                            additionalMeasList.add(AdditionalMeasurements.builder()
                                    .withName(measInfo.getMeasTypes().getSMeasTypesList().get(measResult.getP() - 1))
                                    .withHashMap(hashMap).build());
                        }
                );
        // Adding cell identifier record to additionalMeasList
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

    private Map<String, String> createAdditionalMeasurementHashMap(String value) {
        if (!value.contains(MAP_ENTITY_DELIMITER)) {
            return Collections.singletonMap(VALUE_NAME, value);
        } else {
            return Stream.of(value.split(MAP_ENTITY_DELIMITER))
                           .map(m -> m.split(MAP_VALUES_DELIMITER))
                           .collect(Collectors.toMap(v -> v[0], v -> v.length > 1 ? v[1] : ""));
        }
    }

    /**
     * Creates CommonEventHeader as new copy of initial CommonEventHeader and sets its start/end date by average interval
     *
     * @param fileReadyEvent object created from PM Mapper response
     * @param averageMeasInterval calculated average interval
     * @return created CommonEventHeader
     */
    private CommonEventHeader createEventHeader(FileReadyEvent fileReadyEvent, long averageMeasInterval) {
        CommonEventHeader headerCopy = gson.fromJson(gson.toJson(fileReadyEvent.getCommonEventHeader()), CommonEventHeader.class);
        headerCopy.setStartEpochMicrosec(headerCopy.getStartEpochMicrosec() - averageMeasInterval);
        headerCopy.setLastEpochMicrosec(fileReadyEvent.getCommonEventHeader().getStartEpochMicrosec());
        fileReadyEvent.getCommonEventHeader().setStartEpochMicrosec(headerCopy.getLastEpochMicrosec() + averageMeasInterval);
        return headerCopy;
    }

    /**
     * As MeansType will be selected by its position in the list we need to make sure that MeasTypesList's size is the same size of MeasResults
     *
     * @param measInfo measurement Info object
     * @param measValue measurement Value object
     * @return true=size is the same, false=size is different we can not process it
     */
    private boolean hasListOfTypesSameSizeAsListOfResults(MeasInfo measInfo, MeasValue measValue) {
        return measInfo.getMeasTypes().getSMeasTypesList().size() == measValue.getMeasResults().size();
    }

    /**
     * Average interval between last and start day, divided by number of measurements
     *
     * @param fileReadyEvent object created from PM Mapper response
     * @return Average interval in microseconds
     */
    private long getAverageMeasInterval(FileReadyEvent fileReadyEvent) {
        int noOfMeasurment = fileReadyEvent.getMeasDataCollection().getMeasInfoList().size();
        int dividedBy = (noOfMeasurment == 0 || noOfMeasurment == 1) ? 1 : (noOfMeasurment - 1);
        long difference = fileReadyEvent.getCommonEventHeader().getLastEpochMicrosec() - fileReadyEvent.getCommonEventHeader().getStartEpochMicrosec();
        return difference / dividedBy;
    }

    /**
     * Class which deserialize json event into FileReadyEvent object
     */
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

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(FileReadyEvent.class, new FileReadyEventDeserializer()).create();

}
