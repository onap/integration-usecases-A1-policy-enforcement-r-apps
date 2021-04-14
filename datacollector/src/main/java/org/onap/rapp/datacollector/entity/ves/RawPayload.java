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

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@ToString
@EqualsAndHashCode
@Table("payload")
public class RawPayload {
    @Id
    public final Long eventId;
    public final String payload;

    private RawPayload(Long eventId, String payload) {
        this.eventId = eventId;
        this.payload = payload;
    }

    public static class RawPayloadBuilder {
        @Id
        private Long eventId;
        private String payload;

        public RawPayloadBuilder withEvent(Long event) {
            this.eventId = event;
            return this;
        }

        public RawPayloadBuilder withPayload(String payload) {
            this.payload = payload;
            return this;
        }

        public RawPayload build() {
            return new RawPayload(eventId, payload);
        }
    }

    public static RawPayloadBuilder builder() {
        return new RawPayloadBuilder();
    }
}
