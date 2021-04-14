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
import org.onap.rapp.sleepingcelldetector.configuration.SleepingCellDetectorConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.RicConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.policy.A1PolicyEvent;
import org.onap.rapp.sleepingcelldetector.entity.policy.PolicyInstance;
import org.onap.rapp.sleepingcelldetector.entity.policy.Preference;
import org.onap.rapp.sleepingcelldetector.entity.policy.Resources;
import org.onap.rapp.sleepingcelldetector.entity.policy.Scope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PolicyAgentClientTest {

    @Mock
    SleepingCellDetectorConfiguration config;

    @Mock
    RestTemplate restTemplate;

    @Mock
    JsonHelper jsonHelper;

    private static final String policy = "{\r\n" +
            "  \"scope\" : {\r\n" +
            "    \"ueId\" : \"ue_1\"\r\n" +
            "  },\r\n" +
            "  \"resources\" : [ {\r\n" +
            "    \"cellIdList\" : [ \"Cell1\" ],\r\n" +
            "    \"preference\" : \"AVOID\"\r\n" +
            "  } ]\r\n" +
            "}";

    PolicyAgentClient policyAgentClient;

    @Before
    public void init() throws Exception {
        policyAgentClient = new PolicyAgentClient(config, restTemplate, jsonHelper);
    }

    @Test
    public void sendPolicyTest(){
        PolicyInstance instance = getPolicyInstance();
        RicConfiguration ricConfiguration = getRicConfig();
        Mockito.when(config.getA1PolicyBaseUrl()).thenReturn("http://rapp-datacollector:8087");
        Mockito.when(jsonHelper.objectToJsonString(instance.getJson())).thenReturn(policy);

        policyAgentClient.sendPolicyEvent(instance, ricConfiguration);

        Mockito.verify(config, Mockito.times(1)).getA1PolicyBaseUrl();
        Mockito.verify(restTemplate, Mockito.times(1)).put("http://rapp-datacollector:8087/policy?id=1&ric=ric1&service=rapp-sleepingcelldetector&type=1000",  getHttpEntity(policy));
    }

    @Test
    public void removePolicyTest(){
        Mockito.when(config.getA1PolicyBaseUrl()).thenReturn("http://rapp-datacollector:8087");

        policyAgentClient.deletePolicyInstance("1");

        Mockito.verify(config, Mockito.times(1)).getA1PolicyBaseUrl();
        Mockito.verify(restTemplate, Mockito.times(1)).delete("http://rapp-datacollector:8087/policy?id=1");
    }

    private PolicyInstance getPolicyInstance() {
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

    private RicConfiguration getRicConfig() {
        return new RicConfiguration("ric1", Collections.emptyList(), List.of("1000"), "AVAILABLE");
    }

    private HttpEntity<String> getHttpEntity(String policy) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(policy, headers);
    }
}
