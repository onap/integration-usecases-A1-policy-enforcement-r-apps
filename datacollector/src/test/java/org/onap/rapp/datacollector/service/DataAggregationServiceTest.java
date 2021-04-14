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

package org.onap.rapp.datacollector.service;

import org.onap.rapp.datacollector.entity.DataAggregationInfo;
import org.onap.rapp.datacollector.entity.pm.PMData;
import org.onap.rapp.datacollector.entity.ves.AdditionalMeasurements;
import org.onap.rapp.datacollector.entity.ves.CommonEventHeader;
import org.onap.rapp.datacollector.entity.ves.CommonEventHeaderTest;
import org.onap.rapp.datacollector.entity.ves.Event;
import org.onap.rapp.datacollector.entity.ves.MeasurementFields;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DataAggregationServiceTest {

    public static final int MICRO_SECONDS_OF_SECOND = 1_000_000;

    List<Event> events;
    DataAggregationInfo dataAggregationInfo;

    DataAggregationService dataAggregationService;

    @Before
    public void init() {
        events = new ArrayList<>();
        dataAggregationService = new DataAggregationService();
        dataAggregationInfo = createAggregationInfo();
        long startTime = dataAggregationInfo.getStartTime();


        for (int i = 0; i < 25; i++) {
            CommonEventHeader header = CommonEventHeaderTest.createDumyCommonEventHeaderWithLastEpochMicro(startTime);
            MeasurementFields measurements = createMeasurementFields();
            events.add(Event.of(header, measurements));
            startTime = startTime + MICRO_SECONDS_OF_SECOND;
        }
    }

    private DataAggregationInfo createAggregationInfo() {
        long startTime = Instant.now().getEpochSecond() * MICRO_SECONDS_OF_SECOND;
        return DataAggregationInfo.builder()
                .slot(5 * MICRO_SECONDS_OF_SECOND)
                .startTime(startTime)
                .endTime(startTime + 5 * 5 * MICRO_SECONDS_OF_SECOND)
                .build();
    }

    private MeasurementFields createMeasurementFields() {
        AdditionalMeasurements latency = AdditionalMeasurements.of("latency",
                Map.of("latency", "20"));
        AdditionalMeasurements throughput = AdditionalMeasurements.of("throughput",
                Map.of("throughput", "80"));
        return MeasurementFields.builder()
                .additionalMeasurements(List.of(latency, throughput))
                .build();
    }

    @Test
    public void verifyAggregationData() {
        PMData pmEntity = dataAggregationService.getAggregatedDataFromEventsForCell("Cell1", events, this.dataAggregationInfo);

        pmEntity.getPerformance().forEach(pm ->{
            Assert.assertEquals(Optional.of(pm.getLatency()), Optional.of(20));
            Assert.assertEquals(Optional.of(pm.getThroughput()), Optional.of(80));
        });
    }

    @Test
    public void verifyAggregationDataWithEmptySlotOnBeginning() {
        long startTime = this.dataAggregationInfo.getStartTime() - 10 * MICRO_SECONDS_OF_SECOND;
        DataAggregationInfo dataAggregationInfo = DataAggregationInfo.builder()
                .slot(5 * MICRO_SECONDS_OF_SECOND)
                .startTime(startTime)
                .endTime(startTime + 5 * 6 * MICRO_SECONDS_OF_SECOND)
                .build();

        PMData pmEntity = dataAggregationService.getAggregatedDataFromEventsForCell("Cell1", events, dataAggregationInfo);

        Assert.assertNull(pmEntity.getPerformance().get(0).getLatency());
        Assert.assertNull(pmEntity.getPerformance().get(0).getThroughput());
        Assert.assertEquals(pmEntity.getCellId(), "Cell1");
    }

    @Test
    public void verifyAggregationDataWithEmptySlotOnEnd() {
        long startTime = this.dataAggregationInfo.getStartTime();
        DataAggregationInfo dataAggregationInfo = DataAggregationInfo.builder()
                .slot(5 * MICRO_SECONDS_OF_SECOND)
                .startTime(this.dataAggregationInfo.getStartTime())
                .endTime(startTime + 5 * 6 * MICRO_SECONDS_OF_SECOND)
                .build();

        PMData pmEntity = dataAggregationService.getAggregatedDataFromEventsForCell("Cell1", events, dataAggregationInfo);

        Assert.assertNull(pmEntity.getPerformance().get(5).getLatency());
        Assert.assertNull(pmEntity.getPerformance().get(5).getThroughput());
    }

    @Test
    public void verifyAggregationDataWithEmptySlotInMiddle() {
        removeSecondSlotEvents();
        PMData pmEntity = dataAggregationService.getAggregatedDataFromEventsForCell("Cell1", events, dataAggregationInfo);

        Assert.assertNull(pmEntity.getPerformance().get(1).getLatency());
        Assert.assertNull(pmEntity.getPerformance().get(1).getThroughput());
    }

    private void removeSecondSlotEvents() {
        for (int i = 0; i < 6; i++) {
            events.remove(5);
        }
    }

}
