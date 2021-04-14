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

package org.onap.rapp.sleepingcelldetector;

import org.onap.rapp.sleepingcelldetector.configuration.A1Properties;
import org.onap.rapp.sleepingcelldetector.configuration.DataCollectorProperties;
import org.onap.rapp.sleepingcelldetector.configuration.SleepingCellDetectorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"org.onap.rapp.sleepingcelldetector"})
@EnableConfigurationProperties({A1Properties.class, DataCollectorProperties.class,
        SleepingCellDetectorProperties.class})
@EnableAutoConfiguration
public class SleepingCellDetectorApplication {
    public static void main(String[] args) {
        SpringApplication.run(SleepingCellDetectorApplication.class, args);
    }
}
