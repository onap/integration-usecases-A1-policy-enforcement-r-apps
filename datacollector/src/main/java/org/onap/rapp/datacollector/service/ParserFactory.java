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
