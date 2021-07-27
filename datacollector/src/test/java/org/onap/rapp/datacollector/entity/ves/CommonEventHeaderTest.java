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


package org.onap.rapp.datacollector.entity.ves;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CommonEventHeaderTest {

    public static CommonEventHeader createDumyCommonEventHeader() {
        return CommonEventHeader.builder()
                .domain("domain")
                .eventId("eventId")
                .eventName("eventName")
                .eventType("eventType")
                .lastEpochMicrosec(12345L)
                .nfcNamingCode("nfcNamingCode")
                .nfNamingCode("nfNamingCode")
                .priority("priority")
                .reportingEntityId("entityId")
                .reportingEntityName("reportingEntityName")
                .sequence(567)
                .sourceId("sourceId")
                .sourceName("sourceName")
                .startEpochMicrosec(123456789L)
                .version("version")
                .timeZoneOffset("UTC+2")
                .build();

    }

    public static CommonEventHeader createDumyCommonEventHeaderWithLastEpochMicro(Long lastEpochMicro) {
        return CommonEventHeader.builder()
                .domain("domain")
                .eventId("eventId")
                .eventName("eventName")
                .eventType("eventType")
                .lastEpochMicrosec(lastEpochMicro)
                .nfcNamingCode("nfcNamingCode")
                .nfNamingCode("nfNamingCode")
                .priority("priority")
                .reportingEntityId("entityId")
                .reportingEntityName("reportingEntityName")
                .sequence(567)
                .sourceId("sourceId")
                .sourceName("sourceName")
                .startEpochMicrosec(123456789L)
                .version("version")
                .timeZoneOffset("UTC+2")
                .build();

    }

    @Test
    public void builder() {
        CommonEventHeader actual = createDumyCommonEventHeader();

        assertEquals("version", actual.getVersion());
        assertEquals("domain", actual.getDomain());
        assertEquals("eventId", actual.getEventId());
        assertEquals("eventName", actual.getEventName());
        assertEquals("eventType", actual.getEventType());
        assertEquals(12345L, actual.getLastEpochMicrosec().longValue());
        assertEquals("nfcNamingCode", actual.getNfcNamingCode());
        assertEquals("nfNamingCode", actual.getNfNamingCode());
        assertEquals("priority", actual.getPriority());
        assertEquals("entityId", actual.getReportingEntityId());
        assertEquals("reportingEntityName", actual.getReportingEntityName());
        assertEquals(567, actual.getSequence().intValue());
        assertEquals("sourceId", actual.getSourceId());
        assertEquals("sourceName", actual.getSourceName());
        assertEquals(123456789L, actual.getStartEpochMicrosec().longValue());
        assertEquals("UTC+2", actual.timeZoneOffset);


    }
}