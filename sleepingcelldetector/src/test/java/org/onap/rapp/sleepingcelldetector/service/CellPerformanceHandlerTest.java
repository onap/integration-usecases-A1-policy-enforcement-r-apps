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

package org.onap.rapp.sleepingcelldetector.service;

import org.onap.rapp.sleepingcelldetector.configuration.SleepingCellDetectorConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.RicConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.ue.UEInfo;
import org.onap.rapp.sleepingcelldetector.service.scd.SleepingCellDetectorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class CellPerformanceHandlerTest {

    @Mock
    private SleepingCellDetectorService scdClient;

    @Mock
    private RicConfigurationHolder ricConfigHolder;

    @Mock
    private PolicyInstanceManager policyInstancesManager;

    @Mock
    private SleepingCellDetectorConfiguration config;

    @Mock
    private DataCollectorClient dcClient;

    private CellPerformanceHandler cellPerformanceHandler;

    @Before
    public void init() {
        cellPerformanceHandler = new CellPerformanceHandler(scdClient, ricConfigHolder, policyInstancesManager, config, dcClient);
    }

    @Test
    public void handleActiveCellTest() {
        List<Map<String, Integer>> performanceData = getPMDataList(20, 80);
        Mockito.when(scdClient.isFailing(performanceData)).thenReturn(false);
        Mockito.when(policyInstancesManager.cellContainsPolicy("Cell1")).thenReturn(false);

        cellPerformanceHandler.handleCellPerformance("Cell1", performanceData);

        Mockito.verify(scdClient, Mockito.times(1)).isFailing(performanceData);
        Mockito.verify(policyInstancesManager, Mockito.times(1)).cellContainsPolicy("Cell1");
        Mockito.verify(policyInstancesManager, Mockito.times(0)).removePolicyInstancesForCell("Cell1");
    }

    @Test
    public void handleActiveCellWithPolicyTest() {
        List<Map<String, Integer>> performanceData = getPMDataList(20, 80);
        Mockito.when(scdClient.isFailing( performanceData)).thenReturn(false);
        Mockito.when(policyInstancesManager.cellContainsPolicy("Cell1")).thenReturn(true);

        cellPerformanceHandler.handleCellPerformance("Cell1", performanceData);

        Mockito.verify(scdClient, Mockito.times(1)).isFailing( performanceData);
        Mockito.verify(policyInstancesManager, Mockito.times(1)).cellContainsPolicy("Cell1");
        Mockito.verify(policyInstancesManager, Mockito.times(1)).removePolicyInstancesForCell("Cell1");
    }

    @Test
    public void handleFailingCellTest() {
        List<Map<String, Integer>> performanceData = getPMDataList(300, 2);
        Optional<RicConfiguration> ricConfiguration = getRicConfig();
        Mockito.when(scdClient.isFailing(performanceData)).thenReturn(true);
        Mockito.when(policyInstancesManager.cellContainsPolicyForUe("Cell1", "emergency_samsung_s10_01")).thenReturn(false);
        Mockito.when(policyInstancesManager.cellContainsPolicyForUe("Cell1", "emergency_police_01")).thenReturn(false);
        Mockito.when(ricConfigHolder.getRicConfig()).thenReturn(ricConfiguration);
        Mockito.when(dcClient.getUserEquipment()).thenReturn(getUEInfo());
        Mockito.when(config.getUeFilteringPrefix()).thenReturn("emergency");

        cellPerformanceHandler.handleCellPerformance("Cell1", performanceData);

        Mockito.verify(scdClient, Mockito.times(1)).isFailing( performanceData);
        Mockito.verify(policyInstancesManager, Mockito.times(1)).cellContainsPolicyForUe("Cell1", "emergency_samsung_s10_01");
        Mockito.verify(policyInstancesManager, Mockito.times(1)).cellContainsPolicyForUe("Cell1", "emergency_police_01");
        Mockito.verify(policyInstancesManager, Mockito.times(1)).createPolicyInstance("Cell1", "emergency_samsung_s10_01", ricConfiguration.get());
        Mockito.verify(policyInstancesManager, Mockito.times(1)).createPolicyInstance("Cell1", "emergency_police_01", ricConfiguration.get());
    }

    @Test
    public void handleFailingCellForUeWithPolicyTest() {
        List<Map<String, Integer>> performanceData = getPMDataList(300, 2);
        Optional<RicConfiguration> ricConfiguration = getRicConfig();
        Mockito.when(scdClient.isFailing( performanceData)).thenReturn(true);
        Mockito.when(policyInstancesManager.cellContainsPolicyForUe("Cell1", "emergency_samsung_s10_01")).thenReturn(false);
        Mockito.when(policyInstancesManager.cellContainsPolicyForUe("Cell1", "emergency_police_01")).thenReturn(true);
        Mockito.when(ricConfigHolder.getRicConfig()).thenReturn(ricConfiguration);
        Mockito.when(dcClient.getUserEquipment()).thenReturn(getUEInfo());
        Mockito.when(config.getUeFilteringPrefix()).thenReturn("emergency");

        cellPerformanceHandler.handleCellPerformance("Cell1", performanceData);

        Mockito.verify(scdClient, Mockito.times(1)).isFailing(performanceData);
        Mockito.verify(policyInstancesManager, Mockito.times(1)).cellContainsPolicyForUe("Cell1", "emergency_samsung_s10_01");
        Mockito.verify(policyInstancesManager, Mockito.times(1)).cellContainsPolicyForUe("Cell1", "emergency_police_01");
        Mockito.verify(policyInstancesManager, Mockito.times(1)).createPolicyInstance("Cell1", "emergency_samsung_s10_01", ricConfiguration.get());
        Mockito.verify(policyInstancesManager, Mockito.times(0)).createPolicyInstance("Cell1", "emergency_police_01", ricConfiguration.get());
    }

    @Test
    public void handleFailingCellForEmptyRicConfigTest() {
        List<Map<String, Integer>> performanceData = getPMDataList(300, 2);
        Optional<RicConfiguration> ricConfiguration = getRicConfig();
        Mockito.when(scdClient.isFailing( performanceData)).thenReturn(true);
        Mockito.when(ricConfigHolder.getRicConfig()).thenReturn(Optional.empty());

        cellPerformanceHandler.handleCellPerformance("Cell1", performanceData);

        Mockito.verify(scdClient, Mockito.times(1)).isFailing(performanceData);
        Mockito.verify(policyInstancesManager, Mockito.times(0)).cellContainsPolicyForUe("Cell1", "emergency_samsung_s10_01");
        Mockito.verify(policyInstancesManager, Mockito.times(0)).cellContainsPolicyForUe("Cell1", "emergency_police_01");
        Mockito.verify(policyInstancesManager, Mockito.times(0)).createPolicyInstance("Cell1", "emergency_samsung_s10_01", ricConfiguration.get());
        Mockito.verify(policyInstancesManager, Mockito.times(0)).createPolicyInstance("Cell1", "emergency_police_01", ricConfiguration.get());
    }

    @Test
    public void handleFailingCellForEmptyUeListTest() {
        List<Map<String, Integer>> performanceData = getPMDataList(300, 2);
        Optional<RicConfiguration> ricConfiguration = getRicConfig();
        Mockito.when(scdClient.isFailing( performanceData)).thenReturn(true);
        Mockito.when(ricConfigHolder.getRicConfig()).thenReturn(ricConfiguration);
        Mockito.when(dcClient.getUserEquipment()).thenReturn(new UEInfo(Collections.emptyList()));

        cellPerformanceHandler.handleCellPerformance("Cell1", performanceData);

        Mockito.verify(scdClient, Mockito.times(1)).isFailing(performanceData);
        Mockito.verify(policyInstancesManager, Mockito.times(0)).cellContainsPolicyForUe("Cell1", "emergency_samsung_s10_01");
        Mockito.verify(policyInstancesManager, Mockito.times(0)).cellContainsPolicyForUe("Cell1", "emergency_police_01");
        Mockito.verify(policyInstancesManager, Mockito.times(0)).createPolicyInstance("Cell1", "emergency_samsung_s10_01", ricConfiguration.get());
        Mockito.verify(policyInstancesManager, Mockito.times(0)).createPolicyInstance("Cell1", "emergency_police_01", ricConfiguration.get());
    }

    public static List<Map<String, Integer>> getPMDataList(int latency, int throughput) {
        List<Map<String, Integer>> performanceData = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Map<String, Integer> measurement = new HashMap<>();
            measurement.put("latency", latency);
            measurement.put("throughput", throughput);
            performanceData.add(measurement);
        }
        return performanceData;
    }

    private Optional<RicConfiguration> getRicConfig() {
        return Optional.of(new RicConfiguration("ric1", Collections.emptyList(), List.of("1000"), "AVAILABLE"));
    }

    private UEInfo getUEInfo() {
        return new UEInfo(List.of("emergency_samsung_s10_01", "mobile_samsung_s20_02", "emergency_police_01"));
    }

}
