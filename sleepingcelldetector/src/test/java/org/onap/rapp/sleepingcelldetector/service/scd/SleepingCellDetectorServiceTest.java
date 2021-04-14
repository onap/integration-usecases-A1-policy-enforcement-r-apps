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

package org.onap.rapp.sleepingcelldetector.service.scd;

import org.onap.rapp.sleepingcelldetector.configuration.SleepingCellDetectorConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.MeasurementConfiguration;
import org.onap.rapp.sleepingcelldetector.service.CellPerformanceHandlerTest;
import org.onap.rapp.sleepingcelldetector.service.JsonHelper;
import org.onap.rapp.sleepingcelldetector.service.scd.condition.ConditionEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class SleepingCellDetectorServiceTest {

    private SleepingCellDetectorService sleepingCellDetectorService;

    @Mock
    SleepingCellDetectorConfiguration configuration;

    @Before
    public void init() {
        sleepingCellDetectorService = new SleepingCellDetectorService(new JsonHelper(), configuration);
        sleepingCellDetectorService.measurementMap = prepareMeasurementConfig();
    }

    @Test
    public void predictingActiveCellTestWithFullDataTest() {
        Mockito.when(configuration.getPredictionSlotNumber()).thenReturn(12);
        boolean prediction = sleepingCellDetectorService.isFailing(CellPerformanceHandlerTest.getPMDataList(20, 80));
        Assert.assertFalse(prediction);
    }

    @Test
    public void predictingActiveCellTWithEmptyDataOnBeginningTest() {
        Mockito.when(configuration.getPredictionSlotNumber()).thenReturn(12);
        boolean prediction = sleepingCellDetectorService.isFailing(preparePerformanceDataWithEmptyDataOnBeginning(20, 80));
        Assert.assertFalse(prediction);
    }

    @Test
    public void predictingActiveCellTWithEmptyDataInMiddleTest() {
        Mockito.when(configuration.getPredictionSlotNumber()).thenReturn(12);
        boolean prediction = sleepingCellDetectorService.isFailing(preparePerformanceDataWithEmptyDataInMiddle(20, 80));
        Assert.assertFalse(prediction);
    }

    @Test
    public void predictingActiveCellTWithEmptyDataInEndTest() {
        Mockito.when(configuration.getPredictionSlotNumber()).thenReturn(12);
        boolean prediction = sleepingCellDetectorService.isFailing(preparePerformanceDataWithEmptyDataInEnd(20, 80));
        Assert.assertFalse(prediction);
    }

    @Test
    public void predictingFailingCellTestWithFullDataTest() {
        Mockito.when(configuration.getPredictionSlotNumber()).thenReturn(12);
        boolean prediction = sleepingCellDetectorService.isFailing(CellPerformanceHandlerTest.getPMDataList(500, 5));
        Assert.assertTrue(prediction);
    }

    @Test
    public void predictingFailingCellTWithEmptyDataOnBeginningTest() {
        Mockito.when(configuration.getPredictionSlotNumber()).thenReturn(12);
        boolean prediction = sleepingCellDetectorService.isFailing(preparePerformanceDataWithEmptyDataOnBeginning(500, 5));
        Assert.assertTrue(prediction);
    }

    @Test
    public void predictingFailingCellTWithEmptyDataInMiddleTest() {
        Mockito.when(configuration.getPredictionSlotNumber()).thenReturn(12);
        boolean prediction = sleepingCellDetectorService.isFailing(preparePerformanceDataWithEmptyDataInMiddle(500, 5));
        Assert.assertTrue(prediction);
    }

    @Test
    public void predictingFailingCellTWithEmptyDataInEndTest() {
        Mockito.when(configuration.getPredictionSlotNumber()).thenReturn(12);
        boolean prediction = sleepingCellDetectorService.isFailing(preparePerformanceDataWithEmptyDataInEnd(500, 5));
        Assert.assertTrue(prediction);
    }

    private List<Map<String, Integer>> preparePerformanceDataWithEmptyDataOnBeginning(int latency, int throughput){
        List<Map<String, Integer>> pmList = CellPerformanceHandlerTest.getPMDataList(latency, throughput);
        pmList.set(0, getEmptyMeasurementsData());
        pmList.set(1, getEmptyMeasurementsData());
        pmList.set(2, getEmptyMeasurementsData());
        return  pmList;
    }

    private List<Map<String, Integer>> preparePerformanceDataWithEmptyDataInMiddle(int latency, int throughput){
        List<Map<String, Integer>> pmList = CellPerformanceHandlerTest.getPMDataList(latency, throughput);
        pmList.set(6, getEmptyMeasurementsData());
        pmList.set(7, getEmptyMeasurementsData());
        pmList.set(8, getEmptyMeasurementsData());
        return  pmList;
    }

    private List<Map<String, Integer>> preparePerformanceDataWithEmptyDataInEnd(int latency, int throughput){
        List<Map<String, Integer>> pmList = CellPerformanceHandlerTest.getPMDataList(latency, throughput);
        pmList.set(9, getEmptyMeasurementsData());
        pmList.set(10, getEmptyMeasurementsData());
        pmList.set(11, getEmptyMeasurementsData());
        return  pmList;
    }

    private Map<String, Integer> getEmptyMeasurementsData() {
        Map<String, Integer> emptyDataMap = new HashMap<>();
        emptyDataMap.put("latency", null);
        emptyDataMap.put("throughput", null);
        return emptyDataMap;
    }

    private Map<String, MeasurementConfiguration> prepareMeasurementConfig() {
        MeasurementConfiguration latencyConfig = new MeasurementConfiguration("latency", ConditionEnum.MORE_OR_EQUAL, 400, 150, 2);
        MeasurementConfiguration throughputConfig = new MeasurementConfiguration("throughput", ConditionEnum.LESS_OR_EQUAL, 10, 10, 2);

        Map<String, MeasurementConfiguration> configMap = new HashMap<>();
        configMap.put("latency", latencyConfig);
        configMap.put("throughput", throughputConfig);

        return configMap;
    }
}
