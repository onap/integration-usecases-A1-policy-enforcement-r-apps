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

public class EventTest {

    public static Event createDumyEvent() {
        CommonEventHeader header =
                CommonEventHeaderTest.createDumyCommonEventHeader();
        MeasurementFields fields =
                MeasurementFieldsTest.createDummy(MeasurementFieldsTest.createDummyAdditionalMeasurements());
        return Event.of(header, fields);
    }

    public static Event createDumyEventWithUe() {
        CommonEventHeader header =
                CommonEventHeaderTest.createDumyCommonEventHeader();
        MeasurementFields fields =
                MeasurementFieldsTest.createDummy(MeasurementFieldsTest.createDummyAdditionalMeasurementsWithTrafficModel());
        return Event.of(header, fields);
    }

    @Test
    public void of() {
        CommonEventHeader header =
                CommonEventHeaderTest.createDumyCommonEventHeader();
        MeasurementFields fields =
                MeasurementFieldsTest.createDummy(MeasurementFieldsTest.createDummyAdditionalMeasurements());
        Event actual = Event.of(header, fields);

        assertEquals(header, actual.commonEventHeader);
        assertEquals(fields, actual.measurementFields);
    }

    @Test
    public void testOf() {
        CommonEventHeader header =
                CommonEventHeaderTest.createDumyCommonEventHeader();
        MeasurementFields fields =
                MeasurementFieldsTest.createDummy(MeasurementFieldsTest.createDummyAdditionalMeasurements());
        Event actual = Event.of(12345L, header, fields);

        assertEquals(12345L, actual.id.longValue());
        assertEquals(header, actual.commonEventHeader);
        assertEquals(fields, actual.measurementFields);
    }
}