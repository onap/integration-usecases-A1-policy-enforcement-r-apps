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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@ToString
@Table("additional_measurement")
public class AdditionalMeasurements implements Serializable {
    final Long eventId;
    @Column("am_name")
    public final String name;
    public final List<AdditionalMeasurementValues> values;

    private AdditionalMeasurements(Long eventId, String name, Map<String, String> hashMap) {
        this.eventId = eventId;
        this.name = name;
        this.values = hashMap.keySet()
                .stream()
                .map(key -> AdditionalMeasurementValues.of(this.name, key, hashMap.getOrDefault(key, ""))
                ).collect(Collectors.toUnmodifiableList());
    }

    @JsonCreator
    public static AdditionalMeasurements of(@JsonProperty("name") String name, @JsonProperty("hashMap") Map<String, String> hashMap) {
        return new AdditionalMeasurements(null, name, hashMap);
    }

    public static AdditionalMeasurements of(Long eventId, String name, Map<String, String> hashMap) {
        return new AdditionalMeasurements(eventId, name, hashMap);
    }

    public static AdditionalMeasurementsBuilder builder() {
        return new AdditionalMeasurementsBuilder();
    }

    public static class AdditionalMeasurementsBuilder {
        private long eventId;
        private String name;
        private Map<String, String> hashMap;

        public AdditionalMeasurementsBuilder withEventId(long id) {
            this.eventId = id;
            return this;
        }

        public AdditionalMeasurementsBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public AdditionalMeasurementsBuilder withHashMap(Map<String, String> hashMap) {
            this.hashMap = Collections.unmodifiableMap(hashMap);
            return this;
        }

        public AdditionalMeasurements build() {
            return new AdditionalMeasurements(eventId, name, hashMap);
        }
    }
}
