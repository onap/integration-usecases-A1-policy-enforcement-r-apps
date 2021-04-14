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

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@ToString
@Table("additional_measurement_value")
public class AdditionalMeasurementValues {
    @Column("am_name")
    public final String name;
    @Column("am_key")
    public final String parameterName;
    @Column("am_value")
    public final String parameterValue;

    public AdditionalMeasurementValues(String name, String parameterName, String parameterValue) {
        this.name = name;
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;
    }

    public static AdditionalMeasurementValues of(String name, String parameterName, String parameterValue) {
        return AdditionalMeasurementValues.builder()
                .name(name)
                .parameterName(parameterName)
                .parameterValue(parameterValue)
                .build();
    }
}
