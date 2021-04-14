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
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

@JsonTypeName("event")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table("ves_measurement")
@ToString
@Getter
public class Event {
    @Id
    Long id;

    @Column("rawdata")
    public volatile String raw;

    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    public final CommonEventHeader commonEventHeader;

    @Column("event_id")
    public final MeasurementFields measurementFields;

    protected Event(final Long id, CommonEventHeader header, MeasurementFields fields, String raw) {
        this.id = id;
        this.commonEventHeader = header;
        this.measurementFields = fields;
        this.raw = raw;
    }

    public static Event of(CommonEventHeader header, MeasurementFields fields) {
        return new Event(null, header, fields, "");
    }

    public static Event of(final Long id, CommonEventHeader header, MeasurementFields fields) {
        return new Event(id, header, fields, "");
    }

    public static Event of(final Long id, CommonEventHeader header, MeasurementFields fields, String raw) {
        return new Event(id, header, fields, raw);
    }
}
