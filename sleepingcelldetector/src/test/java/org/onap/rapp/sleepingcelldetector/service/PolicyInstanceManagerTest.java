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
import org.onap.rapp.sleepingcelldetector.entity.RicConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.policy.A1PolicyEvent;
import org.onap.rapp.sleepingcelldetector.entity.policy.PolicyInstance;
import org.onap.rapp.sleepingcelldetector.entity.policy.Preference;
import org.onap.rapp.sleepingcelldetector.entity.policy.Resources;
import org.onap.rapp.sleepingcelldetector.entity.policy.Scope;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PolicyInstanceManagerTest {

    @Mock
    PolicyAgentClient policyAgentClient;

    @Mock
    PolicyInstanceBuilder policyInstanceBuilder;

    PolicyInstanceManager policyInstanceManager;

    @Test
    public void createInstanceTest()  {
        initPolicyInstanceManager();
        RicConfiguration ricConfiguration = getRicConfig();
        PolicyInstance policyInstance = getPolicy(ricConfiguration);
        Mockito.when(policyInstanceBuilder.buildPolicyInstance("Cell1", "ue_1", ricConfiguration))
                .thenReturn(policyInstance);

        Mockito.doNothing().when(policyAgentClient).sendPolicyEvent(policyInstance, ricConfiguration);

        policyInstanceManager.createPolicyInstance("Cell1", "ue_1", ricConfiguration);

        Mockito.verify(policyAgentClient, Mockito.times(1)).sendPolicyEvent(policyInstance, ricConfiguration);
        containsPolicyTest(policyInstanceManager);
    }

    @Test
    public void addInstanceTest(){
        initPolicyInstanceManager();
        RicConfiguration ricConfiguration = getRicConfig();
        PolicyInstance policyInstance = getPolicy(ricConfiguration);

        policyInstanceManager.addPolicyInstance(policyInstance);
        containsPolicyTest(policyInstanceManager);
    }

    @Test
    public void removeInstanceTest(){
        initPolicyInstanceManager();
        RicConfiguration ricConfiguration = getRicConfig();
        PolicyInstance policyInstance = getPolicy(ricConfiguration);

        policyInstanceManager.addPolicyInstance(policyInstance);
        policyInstanceManager.removePolicyInstancesForCell("Cell1");
        Assert.assertFalse(policyInstanceManager.cellContainsPolicy("Cell1"));
        Assert.assertFalse(policyInstanceManager.cellContainsPolicyForUe("Cell1", "ue_1"));
    }


    private void containsPolicyTest(PolicyInstanceManager policyInstanceManager){
        Assert.assertTrue(policyInstanceManager.cellContainsPolicy("Cell1"));
        Assert.assertTrue(policyInstanceManager.cellContainsPolicyForUe("Cell1", "ue_1"));
        Assert.assertFalse(policyInstanceManager.cellContainsPolicy("Cell2"));
        Assert.assertFalse(policyInstanceManager.cellContainsPolicyForUe("Cell2", "ue_1"));
        Assert.assertFalse(policyInstanceManager.cellContainsPolicyForUe("Cell1", "ue_2"));
        Assert.assertFalse(policyInstanceManager.cellContainsPolicyForUe("Cell2", "ue_2"));
    }

    private void initPolicyInstanceManager(){
        policyInstanceManager = new PolicyInstanceManager(policyAgentClient,  policyInstanceBuilder);
    }


    private RicConfiguration getRicConfig() {
        return new RicConfiguration("ric1", Collections.emptyList(), List.of("1000"), "AVAILABLE");
    }

    private PolicyInstance getPolicy(RicConfiguration configuration) {
        Scope scope = new Scope("ue_1");
        Resources resources = new Resources(Lists.newArrayList("Cell1"), Preference.AVOID);
        return PolicyInstance.builder()
                .json(new A1PolicyEvent(scope, Lists.newArrayList(resources)))
                .service("sleepingcelldetector")
                .ric(configuration.getRicName())
                .build();
    }
}
