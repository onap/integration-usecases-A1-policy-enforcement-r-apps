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
import java.util.Map;
import org.junit.Test;

public class AdditionalMeasurementsTest {

    final Map<String, String> m = Map.of("k1", "v1", "k2", "v2");

    @Test
    public void factories() {
        AdditionalMeasurements actual = AdditionalMeasurements.of("test-measurements", m);
        assertEquals("test-measurements", actual.name);
        actual.values.forEach(v -> assertEquals(m.get(v.parameterName), v.parameterValue));

        actual = AdditionalMeasurements.of(1234L, "test-measurements", m);
        assertEquals("test-measurements", actual.name);
        actual.values.forEach(v -> assertEquals(m.get(v.parameterName), v.parameterValue));
        assertEquals(1234L, actual.eventId.longValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutability() {
        AdditionalMeasurements actual = AdditionalMeasurements.builder()
                .withEventId(123L)
                .withName("test-measurements")
                .withHashMap(m)
                .build();
        assertEquals(123L, actual.eventId.longValue());
        assertEquals("test-measurements", actual.name);
        actual.values.forEach(v -> assertEquals(m.get(v.parameterName), v.parameterValue));

        actual.values.add(
                AdditionalMeasurementValues.of("test", "p1", "v1")
        );
    }

}