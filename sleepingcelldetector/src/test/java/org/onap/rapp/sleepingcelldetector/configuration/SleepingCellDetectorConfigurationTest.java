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

package org.onap.rapp.sleepingcelldetector.configuration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ComponentScan("org.onap.rapp.sleepingcelldetector.configuration")
@EnableConfigurationProperties({A1Properties.class, DataCollectorProperties.class,
        SleepingCellDetectorProperties.class})
@ContextConfiguration(classes = {SleepingCellDetectorConfiguration.class})
@TestPropertySource(properties = {"a1.protocol=http", "a1.host=policy-agent", "a1.port=8081",
        "datacollector.protocol=http",
        "datacollector.host=rapp-datacollector",
        "datacollector.port=8087",
        "datacollector.version=v1",
        "sleepingcelldetector.prefix=emergency",
        "sleepingcelldetector.slot=10",
        "sleepingcelldetector.count=12"
})
public class SleepingCellDetectorConfigurationTest {

    @Autowired()
    private SleepingCellDetectorConfiguration config;

    @Test
    public void verifyPolicyAgentUrlConstructionTest() {
        final String actual = config.getA1PolicyBaseUrl();
        final String expected = "http://policy-agent:8081";

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void verifyDataCollectorUrlConstructionTest() {
        final String actual = config.getDataCollectorBaseUrl();
        final String expected = "http://rapp-datacollector:8087/v1";

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void verifySleepingCellDetectorPropertiesTest() {
        Assert.assertEquals(config.getPredictionTimeSlot(), 10);
        Assert.assertEquals(config.getPredictionSlotNumber(), 12);
        Assert.assertEquals(config.getUeFilteringPrefix(), "emergency");
    }
}
