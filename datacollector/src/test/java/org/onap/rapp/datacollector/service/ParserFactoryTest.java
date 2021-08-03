package org.onap.rapp.datacollector.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.onap.rapp.datacollector.TestHelpers.getEmptyEvent;
import static org.onap.rapp.datacollector.TestHelpers.getTestEventFromFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonParseException;

class ParserFactoryTest {

    ParserFactory parserFactory;

    @Mock
    VesParserImpl vesParser;

    @Mock
    FileReadyParserImpl fileReadyParser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        parserFactory = spy(new ParserFactory(vesParser, fileReadyParser));
    }

    @Test
    void testParsedEventsForAllTypes() {
        //File ready event
        parserFactory.getParsedEvents(getTestEventFromFile("/sample-fileready.json"));
        verify(fileReadyParser, times(1)).parse(anyString());

        //VES event
        parserFactory.getParsedEvents(getTestEventFromFile("/sample-ves.json"));
        verify(vesParser, times(1)).parse(anyString());
    }

    @Test
    void testUnspportedJson() {
        String emptyEvent = getEmptyEvent();
        assertThatExceptionOfType(JsonParseException.class).isThrownBy(() -> parserFactory.getParsedEvents(emptyEvent));
    }

}