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

import java.util.Collections;
import java.util.List;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
@Builder
@Table("ves_measurement_fields")
public class MeasurementFields {
    public static final MeasurementFields EMPTY = new MeasurementFields(-1L, -1L, Collections.emptyList());
    public final Long eventId;
    public final long measurementInterval;
    public static final String MEASUREMENT_FIELDS_VERSION = "4.0";

    @Column("event_id")
    public final List<AdditionalMeasurements> additionalMeasurements;

    private MeasurementFields(Long eventId, long measurementInterval, List<AdditionalMeasurements> additionalMeasurements) {
        this.eventId = eventId;
        this.measurementInterval = measurementInterval;
        this.additionalMeasurements = Collections.unmodifiableList(additionalMeasurements);
    }

    public static MeasurementFields  of(Long eventId) {
        return new MeasurementFields(eventId, -1, Collections.emptyList());
    }
}
