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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import org.onap.rapp.datacollector.entity.ves.AdditionalMeasurements;
import org.onap.rapp.datacollector.entity.ves.CommonEventHeader;
import org.onap.rapp.datacollector.entity.ves.Event;
import org.onap.rapp.datacollector.entity.ves.MeasurementFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VesParserImpl implements VesParser {
    private static final Logger logger = LoggerFactory.getLogger(VesParserImpl.class);

    private static class VesEventDeserializer implements JsonDeserializer<Event> {
        private static class AdditionalMeasurementsRawValue {
            String name;
            Map<String, String> hashMap;
        }

        @Override
        public Event deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject obj = jsonElement.getAsJsonObject();
            obj = obj.getAsJsonObject("event");
            CommonEventHeader header;
            Optional<MeasurementFields> measurementFields = Optional.empty();
            List<AdditionalMeasurements> additionalMeasurements = Collections.emptyList();
            if (obj.has("commonEventHeader")) {
                JsonObject h = obj.getAsJsonObject("commonEventHeader");
                header = jsonDeserializationContext.deserialize(h, CommonEventHeader.class);
            } else {
                throw new JsonParseException("Common header not found");
            }
            if (obj.has("measurementFields")) {
                JsonObject h = obj.getAsJsonObject("measurementFields");
                measurementFields = Optional.ofNullable(jsonDeserializationContext.deserialize(h, MeasurementFields.class));
                if (h.has("additionalMeasurements")) {
                    JsonArray arr = h.getAsJsonArray("additionalMeasurements");
                    additionalMeasurements = new ArrayList<>();
                    for (int i = 0; i < arr.size(); i++) {
                        AdditionalMeasurementsRawValue tmp = jsonDeserializationContext.deserialize(arr.get(i).getAsJsonObject(), AdditionalMeasurementsRawValue.class);
                        additionalMeasurements.add(AdditionalMeasurements.builder()
                                .withName(tmp.name)
                                .withHashMap(tmp.hashMap)
                                .build());
                    }
                }
            }
            logger.trace("measurement fields {}", measurementFields);
            logger.trace("additional measurements {}", additionalMeasurements);
            measurementFields = Optional.of(MeasurementFields.builder()
                    .measurementInterval(measurementFields.orElse(MeasurementFields.EMPTY).measurementInterval)
                    .additionalMeasurements(additionalMeasurements)
                    .build());

            return Event.of(header, measurementFields.get());
        }
    }

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Event.class, new VesEventDeserializer())
            .create();

    public Event parse(final String event) {
        logger.debug("parsing ves event {}", event);
        final Event result = gson.fromJson(event, Event.class);
        result.raw = event;
        return result;
    }

}
