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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.rapp.sleepingcelldetector.entity.pm.PMEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CellPerformanceVerifierScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CellPerformanceVerifierScheduler.class);

    private final DataCollectorClient dataCollectorClient;
    private final CellPerformanceHandler cellPerformanceHandler;

    public CellPerformanceVerifierScheduler(DataCollectorClient dataCollectorClient, CellPerformanceHandler cellPerformanceHandler) {
        this.dataCollectorClient = dataCollectorClient;
        this.cellPerformanceHandler = cellPerformanceHandler;
    }

    @Scheduled(fixedRateString = "${scd.slot}000")
    public void handleMeasurements() {
        logger.debug("Perform measurements check");
        performVesEventsMeasurementVerification();
    }

    private void performVesEventsMeasurementVerification() {
        try {
            PMEntity performanceData =  dataCollectorClient.getPMData();
            Map<String, List<Map<String, Integer>>> eventsByCell = groupByCell(performanceData);
            eventsByCell.forEach(cellPerformanceHandler::handleCellPerformance);
        } catch (Exception e) {
            logger.error("Error occurred during events verification, message: {}", e.getMessage());
        }
    }

    private Map<String, List<Map<String, Integer>>> groupByCell(PMEntity pmEntity) {
        Map<String, List<Map<String, Integer>>> cellPerformanceMap = new HashMap<>();
        pmEntity.getPm().forEach(pmData -> cellPerformanceMap.put(pmData.getCellId(), pmData.getPerformance()));
        return cellPerformanceMap;
    }

}
