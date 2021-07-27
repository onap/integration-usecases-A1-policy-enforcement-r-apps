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

import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonEventHeader {

    private final String eventType;
    private final String version;
    private final String sourceId;
    private final String reportingEntityName;
    private Long startEpochMicrosec;
    private final String eventId;
    private Long lastEpochMicrosec;
    private final String priority;
    private final Integer sequence;
    private final String sourceName;
    private final String domain;
    private final String eventName;
    private final String reportingEntityId;
    private final String nfcNamingCode;
    private final String nfNamingCode;
    @Transient
    public final String timeZoneOffset;

}
