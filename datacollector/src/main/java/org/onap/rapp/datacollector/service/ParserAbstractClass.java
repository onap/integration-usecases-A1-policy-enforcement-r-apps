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

import java.util.Optional;

import org.onap.rapp.datacollector.entity.ves.CommonEventHeader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public abstract class ParserAbstractClass {

    public static final String VES_EVENT_UNIQUE_ELEMENT = "measurementFields";
    public static final String FILE_READY_EVENT_UNIQUE_ELEMENT = "perf3gppFields";
    public static final String EVENT_JSON_ELEMENT_NAME = "event";
    public static final String COMMON_EVENT_HEADER = "commonEventHeader";

    /**
     * Scans the Json Object if contains VES_EVENT_UNIQUE_ELEMENT
     *
     * @param obj json object
     * @return true=it is VES event, false=not VES event
     */
    protected boolean isVesEvent(JsonObject obj) {
        return getEventJsonObject(obj).filter(jsonObject -> jsonObject.has(VES_EVENT_UNIQUE_ELEMENT)).isPresent();
    }

    /**
     * Scans the Json Object if contains FILE_READY_EVENT_UNIQUE_ELEMENT
     *
     * @param obj json object
     * @return true=it is FileReadyEvent event, false=not FileReadyEvent
     */
    protected boolean isFileReadyEvent(JsonObject obj) {
        return getEventJsonObject(obj).filter(jsonObject -> jsonObject.has(FILE_READY_EVENT_UNIQUE_ELEMENT)).isPresent();
    }

    /**
     * Gets Event json element from incoming json
     *
     * @param jsonElement top json elemnt
     * @return Event Json element
     */
    protected static Optional<JsonObject> getEventJsonObject(JsonElement jsonElement) {
        JsonObject obj = jsonElement.getAsJsonObject();
        return Optional.ofNullable(obj.getAsJsonObject(EVENT_JSON_ELEMENT_NAME));
    }

    /**
     * Gets CommonEventHeader from json
     *
     * @param obj Event json element
     * @param jsonDeserializationContext json context
     * @return CommonEventHeader object from json object
     */
    protected static CommonEventHeader getHeaderJsonObject(JsonObject obj, JsonDeserializationContext jsonDeserializationContext) {
        if (nonNull(obj) && obj.has(COMMON_EVENT_HEADER)) {
            JsonObject headerJson = obj.getAsJsonObject(COMMON_EVENT_HEADER);
            return jsonDeserializationContext.deserialize(headerJson, CommonEventHeader.class);
        } else {
            throw new JsonParseException("Common header not found");
        }
    }

}
