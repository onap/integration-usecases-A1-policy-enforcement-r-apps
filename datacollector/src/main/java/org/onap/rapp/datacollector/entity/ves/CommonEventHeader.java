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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

@Data
@EqualsAndHashCode
@ToString
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonEventHeader {
    public final String eventType;
    public final String version;
    public final String sourceId;
    public final String reportingEntityName;
    public final Long startEpochMicrosec;
    public final String eventId;
    public final Long lastEpochMicrosec;
    public final String priority;
    public final Integer sequence;
    public final String sourceName;
    public final String domain;
    public final String eventName;
    public final String reportingEntityId;
    public final String nfcNamingCode;
    public final String nfNamingCode;
    @Transient
    public final String timeZoneOffset;

    protected CommonEventHeader(String eventType, String version, String sourceId, String reportingEntityName, Long startEpochMicrosec, String eventId, Long lastEpochMicrosec, String priority, Integer sequence, String sourceName, String domain, String eventName, String reportingEntityId, String nfcNamingCode, String nfNamingCode, String timeZone) {
        this.eventType = eventType;
        this.version = version;
        this.sourceId = sourceId;
        this.reportingEntityName = reportingEntityName;
        this.startEpochMicrosec = startEpochMicrosec;
        this.eventId = eventId;
        this.lastEpochMicrosec = lastEpochMicrosec;
        this.priority = priority;
        this.sequence = sequence;
        this.sourceName = sourceName;
        this.domain = domain;
        this.eventName = eventName;
        this.reportingEntityId = reportingEntityId;
        this.nfcNamingCode = nfcNamingCode;
        this.nfNamingCode = nfNamingCode;
        this.timeZoneOffset = timeZone;
    }
}
