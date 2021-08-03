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

import org.junit.Before;
import org.junit.Test;
import org.onap.rapp.datacollector.entity.ves.Event;

import com.google.gson.JsonParseException;

public class VesParserImplTest {

    String testVesContent;
    VesParser parser = new VesParserImpl();

    @Before
    public void setUp() {
        testVesContent = getTestEventFromFile("/sample-ves.json");
    }

    @Test
    public void testParsing() {
        Event actual = parser.parse(testVesContent).get(0);
        assertEquals("4.0.1", actual.commonEventHeader.getVersion());
        assertEquals(1413378172000000L, (long) actual.commonEventHeader.getLastEpochMicrosec());
        assertEquals(1413378172000000L, (long) actual.commonEventHeader.getStartEpochMicrosec());
        assertEquals(3, (int) actual.commonEventHeader.getSequence());
        assertEquals("measurement", actual.commonEventHeader.getDomain());
        assertEquals("UTC-05:30", actual.commonEventHeader.timeZoneOffset);
    }

    @Test(expected = JsonParseException.class)
    public void parseEmpty() {
        parser.parse(getEmptyEvent());
    }
}