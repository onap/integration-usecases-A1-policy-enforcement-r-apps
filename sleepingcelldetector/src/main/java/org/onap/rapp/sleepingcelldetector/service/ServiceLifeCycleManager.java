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

import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ServiceLifeCycleManager {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLifeCycleManager.class);
    private final PolicyAgentClient policyAPIClient;

    public ServiceLifeCycleManager(PolicyAgentClient policyAPIClient) {
        this.policyAPIClient = policyAPIClient;
    }

    @PostConstruct
    private void createService() {
        logger.info("Registering policy actor service");

        boolean serviceCreated = false;
        while (!serviceCreated) {
            try {
                serviceCreated = policyAPIClient.createService();
                TimeUnit.SECONDS.sleep(10);
            } catch (Exception e) {
                logger.error("Error during service registration {}", e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 20000)
    private void keepAlive() {
        try {
            logger.debug("Send keep alive request");
            policyAPIClient.sendKeepAliveRequest();
        } catch (Exception e) {
            logger.warn("Error during keep alive request {}", e.getMessage());
            createService();
        }

    }
}
