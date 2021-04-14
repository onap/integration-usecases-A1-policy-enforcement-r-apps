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

import java.util.Optional;
import org.onap.rapp.sleepingcelldetector.entity.RicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RicConfigurationHolder {

    private static final Logger logger = LoggerFactory.getLogger(RicConfigurationHolder.class);

    private RicConfiguration ricConfiguration;

    public void addRic(RicConfiguration ricConfig, String policyTypeId) {
        logger.info("Adding ric configuration for Policy Type {}, config: {}", policyTypeId, ricConfig);
        ricConfiguration = ricConfig;
    }

    public Optional<RicConfiguration> getRicConfig() {
        return Optional.of(ricConfiguration);
    }
}
