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
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.onap.rapp.sleepingcelldetector.entity.RicConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.policy.PolicyInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RicConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(RicConfigurationManager.class);

    private final RicConfigurationHolder ricConfigurationHolder;
    private final PolicyInstanceManager policyInstanceManager;
    private final PolicyAgentClient policyAgentClient;

    public RicConfigurationManager(RicConfigurationHolder ricConfigurationHolder, PolicyAgentClient policyAgentClient,
                                   PolicyInstanceManager policyInstanceManager
    ) {
        this.ricConfigurationHolder = ricConfigurationHolder;
        this.policyAgentClient = policyAgentClient;
        this.policyInstanceManager = policyInstanceManager;
    }

    @PostConstruct
    private void getRicConfigurations() throws InterruptedException {
        logger.info("Policy fetching process started");
        boolean ricConfigUploadStatus = false;
        while (!ricConfigUploadStatus) {
            ricConfigUploadStatus = getRicConfigs();
            TimeUnit.SECONDS.sleep(10);
        }
    }

    private boolean getRicConfigs() {
        try {
            List<String> policyIds = policyAgentClient.getPoliciesIds();

            if (policyIds.isEmpty()) {
                logger.warn("Problems with ric configuration fetching; Next try in 10 sec");
                return false;
            }

            saveRicConfiguration(policyIds);
            policyIds.forEach(this::verifyExistingPolicyInstances);
            logger.info("Policy fetching process ended successfully");
            return true;
        } catch (Exception e) {
            logger.error("Problems with ric configuration fetching; Next try in 10 sec: " + e.getMessage());
            return false;
        }
    }

    private void saveRicConfiguration(List<String> policyIds) {
        policyIds.forEach(policyId -> {
            logger.info("Saving ric configurations for policyId {}", policyId);
            List<RicConfiguration> ricConfigs = policyAgentClient.getRicConfigurationsByPolicyId(policyId);
            ricConfigs.forEach(rc -> ricConfigurationHolder.addRic(rc, policyId));
        });
    }

    private void verifyExistingPolicyInstances(String policyTypeId) {
        List<PolicyInstance> policyInstances = policyAgentClient.getPoliciesInstances();
        if (policyInstances.isEmpty()) {
            logger.info("No policy instances of type {}", policyTypeId);
        } else {
            policyInstances.stream()
                    .filter(pi -> pi.getType().equals(policyTypeId))
                    .forEach(this::addPolicyInstance);
        }
    }

    private void addPolicyInstance(PolicyInstance pi) {
        policyInstanceManager.addPolicyInstance(pi);
        logger.info("Policy Instance of type {} and id {} added on startup", pi.getType(), pi.getId());
    }

}
