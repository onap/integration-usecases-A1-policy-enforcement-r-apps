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

import java.util.List;

import org.onap.rapp.datacollector.entity.ves.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Factory which decided what parser to use to deserialize json event coming from PM Mapper Supported are FileReadyEvent and VES event
 */
@Service
public class ParserFactory extends ParserAbstractClass {

    private static final Logger logger = LoggerFactory.getLogger(ParserFactory.class);
    private final VesParserImpl vesParser;
    private final FileReadyParserImpl fileReadyParser;

    public ParserFactory(VesParserImpl vesParser, FileReadyParserImpl fileReadyParser) {
        this.vesParser = vesParser;
        this.fileReadyParser = fileReadyParser;
    }

    public List<Event> getParsedEvents(String eventString) {
        JsonObject json = gson.fromJson(eventString, JsonObject.class);
        if (isFileReadyEvent(json)) {
            return fileReadyParser.parse(eventString);
        } else if (isVesEvent(json)) {
            return vesParser.parse(eventString);
        } else {
            logger.error("Not supported event structure {}", eventString);
            throw new JsonParseException("Not supported event structure");
        }
    }

    private final Gson gson = new GsonBuilder().create();
}
