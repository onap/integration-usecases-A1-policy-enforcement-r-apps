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

import com.google.common.collect.Lists;
import org.onap.rapp.sleepingcelldetector.entity.MeasurementConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.policy.A1PolicyEvent;
import org.onap.rapp.sleepingcelldetector.entity.policy.PolicyInstance;
import org.onap.rapp.sleepingcelldetector.entity.policy.Preference;
import org.onap.rapp.sleepingcelldetector.entity.policy.Resources;
import org.onap.rapp.sleepingcelldetector.entity.policy.Scope;
import org.onap.rapp.sleepingcelldetector.service.scd.condition.ConditionEnum;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonHelperTest {

    private static final String policy = "{\r\n" +
            "  \"id\" : \"1\",\r\n" +
            "  \"type\" : \"1000\",\r\n" +
            "  \"ric\" : \"ric1\",\r\n" +
            "  \"service\" : \"sleepingcelldetector\",\r\n" +
            "  \"lastModified\" : null,\r\n" +
            "  \"json\" : {\r\n" +
            "    \"scope\" : {\r\n" +
            "      \"ueId\" : \"ue_1\"\r\n" +
            "    },\r\n" +
            "    \"resources\" : [ {\r\n" +
            "      \"cellIdList\" : [ \"Cell1\" ],\r\n" +
            "      \"preference\" : \"AVOID\"\r\n" +
            "    } ]\r\n" +
            "  }\r\n" +
            "}";

    private static final String tcaConfig = "[\n" +
            "  {\n" +
            "    \"name\": \"latency\",\n" +
            "    \"condition\": \"MORE_OR_EQUAL\",\n" +
            "    \"averageThresholdValue\": 400,\n" +
            "    \"latestThresholdValue\": 150,\n" +
            "    \"latestSize\" : 2\n" +
            "  },\n" +
            "  {\n" +
            "    \"name\": \"throughput\",\n" +
            "    \"condition\": \"LESS_OR_EQUAL\",\n" +
            "    \"averageThresholdValue\": 10,\n" +
            "    \"latestThresholdValue\": 10,\n" +
            "    \"latestSize\" : 2\n" +
            "  }\n" +
            "]";

    JsonHelper jsonHelper;

    @Before()
    public void initialSetUp() {
        jsonHelper = new JsonHelper();
    }

    @Test
    public void policyToJsonStringTest() {
        PolicyInstance instance =  getPolicyToCompare();
        assertThat(policy).isEqualToNormalizingNewlines(jsonHelper.objectToJsonString(instance));
    }

    @Test
    public void tcaConfigDeserializeTest() {
        MeasurementConfiguration[] configurations = jsonHelper.deserialize(tcaConfig, MeasurementConfiguration[].class);
        assertThat(configurations).isEqualTo(prepareTcaConfig());
    }

    private PolicyInstance getPolicyToCompare() {
        Scope scope = new Scope("ue_1");
        Resources resources = new Resources(Lists.newArrayList("Cell1"), Preference.AVOID);
        return PolicyInstance.builder()
                .json(new A1PolicyEvent(scope, Lists.newArrayList(resources)))
                .service("sleepingcelldetector")
                .ric("ric1")
                .id("1")
                .type("1000")
                .build();
    }

    private MeasurementConfiguration[] prepareTcaConfig() {
        MeasurementConfiguration latencyConfig = new MeasurementConfiguration("latency", ConditionEnum.MORE_OR_EQUAL, 400, 150, 2);
        MeasurementConfiguration throughputConfig = new MeasurementConfiguration("throughput", ConditionEnum.LESS_OR_EQUAL, 10, 10, 2);
        return new MeasurementConfiguration[]{latencyConfig, throughputConfig};
    }

}
