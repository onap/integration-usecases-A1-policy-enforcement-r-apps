package org.onap.rapp.datacollector.service;

import static org.junit.Assert.assertEquals;
import static org.onap.rapp.datacollector.TestHelpers.getEmptyEvent;
import static org.onap.rapp.datacollector.TestHelpers.getTestEventFromFile;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onap.rapp.datacollector.entity.ves.Event;

import com.google.gson.JsonParseException;

public class FileReadyParserImplTest {

    String testFileReadyContent;
    VesParser parser = new FileReadyParserImpl();

    @Before
    public void setUp() {
        testFileReadyContent = getTestEventFromFile("/sample-fileready.json");
    }

    @Test
    public void testParsing() {
        List<Event> listOfEvents = parser.parse(testFileReadyContent);
        assertEquals(4, listOfEvents.size());
        listOfEvents.forEach(event -> {
            assertEquals("4.0", event.getCommonEventHeader().getVersion());
            assertEquals("perf3gpp", event.getCommonEventHeader().getDomain());
            assertEquals("perf3gpp_PE-Samsung_pmMeasResult", event.getCommonEventHeader().getEventName());
            assertEquals(3, event.getMeasurementFields().getAdditionalMeasurements().size());
        });
    }

    @Test(expected = JsonParseException.class)
    public void parseEmpty() {
        parser.parse(getEmptyEvent());
    }
}