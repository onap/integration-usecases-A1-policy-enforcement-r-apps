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
        testFileReadyContent = getTestEventFromFile("/sample-fileready.txt");
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