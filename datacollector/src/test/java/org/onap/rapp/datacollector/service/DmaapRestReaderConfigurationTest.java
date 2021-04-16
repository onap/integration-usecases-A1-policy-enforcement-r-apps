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


package org.onap.rapp.datacollector.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.rapp.datacollector.service.configuration.DatabaseProperties;
import org.onap.rapp.datacollector.service.configuration.DmaapProperties;
import org.onap.rapp.datacollector.service.configuration.DmaapRestReaderConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ComponentScan("org.onap.rapp.datacollector.service.configuration")
@EnableConfigurationProperties({DmaapProperties.class, DatabaseProperties.class})
@ContextConfiguration(classes = {DmaapRestReaderConfiguration.class})
@TestPropertySource(properties = {"dmaap.host=localhost",
        "dmaap.protocol=http",
        "dmaap.port=8080",
        "dmaap.measurements-topic=a-topic",
        "database.url=jdbc:mysql://172.17.0.2:3306/ves?createDatabaseIfNotExist=true",
        "database.username=root",
        "database.password=mypass",
        "database.driver-class-name=org.mariadb.jdbc.Driver"
})

public class DmaapRestReaderConfigurationTest {

    @Autowired
    private DmaapRestReaderConfiguration config;

    @Test
    public void testUrlConstruction() {
        final String actual = config.getMeasurementsTopicUrl();
        final String expected = "http://localhost:8080/a-topic";

        assertEquals(expected, actual);
    }
}
