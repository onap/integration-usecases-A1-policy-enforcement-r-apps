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
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class MeasurementFieldsTest {
    static AdditionalMeasurements createDummyAdditionalMeasurements() {
        return AdditionalMeasurements.builder()
                .withName("test-measurements")
                .withHashMap(Map.of("k1", "v1", "k2", "v2", "k3", "v3"))
                .build();
    }

    public static AdditionalMeasurements createDummyAdditionalMeasurementsWithTrafficModel() {
        return AdditionalMeasurements.builder()
                .withName("trafficModel")
                .withHashMap(Map.of("emergency_samsung_01", "v1", "mobile_samsung_s10", "v2"))
                .build();
    }

    static MeasurementFields createDummy(AdditionalMeasurements v) {
        return MeasurementFields.builder()
                .measurementInterval(1234567L)
                .additionalMeasurements(List.of(v))
                .build();

    }

    @Test
    public void test() {
        AdditionalMeasurements v = createDummyAdditionalMeasurements();
        MeasurementFields actual = createDummy(v);

        assertEquals(1234567L, actual.measurementInterval);
        assertEquals("4.0", actual.measurementFieldsVersion);
        assertEquals(List.of(v), actual.additionalMeasurements);
    }
}
