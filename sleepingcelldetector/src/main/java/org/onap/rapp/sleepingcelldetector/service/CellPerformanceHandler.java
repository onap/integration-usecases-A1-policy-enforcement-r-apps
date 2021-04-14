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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.onap.rapp.sleepingcelldetector.configuration.SleepingCellDetectorConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.RicConfiguration;
import org.onap.rapp.sleepingcelldetector.service.scd.SleepingCellDetectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CellPerformanceHandler {

    private static final Logger logger = LoggerFactory.getLogger(CellPerformanceHandler.class);

    private final SleepingCellDetectorService sleepingCellDetectorService;
    private final RicConfigurationHolder ricConfigHolder;
    private final PolicyInstanceManager policyInstancesManager;
    private final SleepingCellDetectorConfiguration config;
    private final DataCollectorClient dataCollectorClient;

    public CellPerformanceHandler(SleepingCellDetectorService sleepingCellDetectorService, RicConfigurationHolder ricConfigHolder,
                                  PolicyInstanceManager policyInstanceManager, SleepingCellDetectorConfiguration config,
                                  DataCollectorClient dataCollectorClient) {
        this.sleepingCellDetectorService = sleepingCellDetectorService;
        this.ricConfigHolder = ricConfigHolder;
        this.policyInstancesManager = policyInstanceManager;
        this.config = config;
        this.dataCollectorClient = dataCollectorClient;
    }

    public void handleCellPerformance(String cell, List<Map<String, Integer>> pmData) {
        logger.info("Handle cell: {} started", cell);
        try {
            if (pmData.isEmpty()) {
                logger.warn("pm data is empty for cell: {}", cell);
                return;
            }

            Boolean isFailing = sleepingCellDetectorService.isFailing(pmData);
            handleCell(cell, isFailing);
        } catch (Exception e) {
            logger.error("Error during sleeping cell detecting: {}", e.getMessage());
        }
    }

    private void handleCell(String cell, boolean isFailing) {
        if (isFailing) {
            handleFailingCell(cell);
        } else {
            handleActiveCell(cell);
        }
    }

    private void handleFailingCell(String cell) {
        logger.info("Cell {} is in failed status, policy instance will be created", cell);

        Optional<RicConfiguration> ricConfig = ricConfigHolder.getRicConfig();
        if (ricConfig.isEmpty()) {
            logger.warn("Can't find ric configuration for cell: {}", cell);
            return;
        }

        Set<String> userEquipmentsIds = getHighPriorityUEs();
        if (userEquipmentsIds.isEmpty()) {
            logger.warn("No high priority user equipment for cell: {}", cell);
            return;
        }

        createPolicyInstance(ricConfig.get(), userEquipmentsIds, cell);
    }

    private Set<String> getHighPriorityUEs() {
        return dataCollectorClient.getUserEquipment().getUes().stream()
                .filter(ue -> ue.startsWith(config.getUeFilteringPrefix()))
                .collect(Collectors.toSet());
    }

    private void createPolicyInstance(RicConfiguration ricConfig, Set<String> userEquipmentsIds, String cell) {
        userEquipmentsIds.stream()
                .filter(ue -> !policyInstancesManager.cellContainsPolicyForUe(cell, ue))
                .forEach(ue -> policyInstancesManager.createPolicyInstance(cell, ue, ricConfig));
    }

    private void handleActiveCell(String cell) {
        if (policyInstancesManager.cellContainsPolicy(cell)) {
            policyInstancesManager.removePolicyInstancesForCell(cell);
        }
        logger.info("Cell {} is not in failed status", cell);
    }

}
