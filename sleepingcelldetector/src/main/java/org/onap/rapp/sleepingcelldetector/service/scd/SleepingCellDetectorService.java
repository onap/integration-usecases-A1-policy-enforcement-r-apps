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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.onap.rapp.sleepingcelldetector.configuration.SleepingCellDetectorConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.MeasurementConfiguration;
import org.onap.rapp.sleepingcelldetector.service.JsonHelper;
import org.onap.rapp.sleepingcelldetector.service.scd.condition.Condition;
import org.onap.rapp.sleepingcelldetector.service.scd.condition.ConditionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SleepingCellDetectorService {

    private static final Logger logger = LoggerFactory.getLogger(SleepingCellDetectorService.class);

    private final JsonHelper jsonHelper;
    private final SleepingCellDetectorConfiguration configuration;

    protected Map<String, MeasurementConfiguration> measurementMap = new HashMap<>();

    @Autowired
    public SleepingCellDetectorService(JsonHelper jsonHelper, SleepingCellDetectorConfiguration configuration) {
        this.jsonHelper = jsonHelper;
        this.configuration = configuration;
    }

    @PostConstruct
    public void loadConditionMap() throws Exception {
        logger.info("TCA config fetching process started");
        boolean tcaConfigUploadStatus = false;
        while (!tcaConfigUploadStatus) {
            tcaConfigUploadStatus = getTCAConfig();
            TimeUnit.SECONDS.sleep(10);
        }
    }

    private boolean getTCAConfig() {
        try {
            InputStream in = this.getClass().getResourceAsStream("/tca.json");
            BufferedReader inr = new BufferedReader(new InputStreamReader(in));
            String tcaConfig = inr.lines().collect(Collectors.joining(" "));

            MeasurementConfiguration[] measurementConfigurations = jsonHelper.deserialize(tcaConfig, MeasurementConfiguration[].class);
            Arrays.stream(measurementConfigurations).forEach(mf -> measurementMap.put(mf.getName(), mf));
            return true;
        } catch (Exception e) {
            logger.error("Error during loading Measurement Configuration for TCA: {}", e.getMessage());
            return false;
        }
    }

    public Boolean isFailing(List<Map<String, Integer>> performanceData) {
        Map<String, List<Integer>> measurementValuesMap = groupMeasurementValues(performanceData);

        measurementValuesMap.forEach((fieldName, values) -> {
            if (containsEmptyValues(values)) {
                CalculationUtil.fillGaps(values);
            }
        });

        List<Boolean> predictions = new ArrayList<>();
        measurementValuesMap.forEach((fieldName, values) -> predictions.add(isFailing(values, measurementMap.get(fieldName))));

        return predictions.contains(true);
    }

    private Map<String, List<Integer>> groupMeasurementValues(List<Map<String, Integer>> performanceData) {
        Map<String, List<Integer>> measurementValuesMap = prepareMeasurementMap();

        performanceData.forEach(pm -> measurementMap.forEach((fieldName, configuration) -> {
            Integer measurement = pm.get(fieldName);
            List<Integer> measurements = measurementValuesMap.get(fieldName);
            measurements.add(measurement);
        }));

        return measurementValuesMap;
    }

    private Map<String, List<Integer>> prepareMeasurementMap() {
        Map<String, List<Integer>> measurementValuesMap = new HashMap<>();
        measurementMap.forEach((fieldName, values) -> measurementValuesMap.put(fieldName, new ArrayList<>()));
        return measurementValuesMap;
    }

    private boolean containsEmptyValues(List<Integer> performanceData) {
        for (Integer value : performanceData) {
            if (value == null) {
                return true;
            }
        }
        return false;
    }

    private Boolean isFailing(List<Integer> values, MeasurementConfiguration measurementConfiguration) {
        Condition condition = ConditionFactory.getCondition(measurementConfiguration.getCondition());
        Integer totalAverage = CalculationUtil.calculateAverage(values);
        Integer latestAverage = CalculationUtil.calculateAverage(values.stream()
                .skip(configuration.getPredictionSlotNumber() - measurementConfiguration.getLatestSize())
                .collect(Collectors.toList()));

        return condition.compare(totalAverage, measurementConfiguration.getAverageThresholdValue())
        && condition.compare(latestAverage, measurementConfiguration.getLatestThresholdValue());
    }

}
