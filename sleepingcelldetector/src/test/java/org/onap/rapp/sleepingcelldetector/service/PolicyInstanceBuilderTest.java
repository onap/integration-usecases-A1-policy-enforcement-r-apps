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

import java.util.Collections;
import java.util.List;

public class PolicyInstanceBuilderTest {

    private PolicyInstanceBuilder policyInstanceBuilder;

   @Test
    public void buildPolicyInstanceTest(){
       policyInstanceBuilder = new PolicyInstanceBuilder();
       PolicyInstance policyToCompare = getPolicyToCompare();
       RicConfiguration ricConfiguration = getRicConfig();

       PolicyInstance policy = policyInstanceBuilder.buildPolicyInstance("Cell1", "ue_1", ricConfiguration);

       Assert.assertEquals(policy.getJson(), policyToCompare.getJson());
       Assert.assertEquals(policy.getLastModified(), policyToCompare.getLastModified());
       Assert.assertEquals(policy.getService(), policyToCompare.getService());
       Assert.assertEquals(policy.getRic(), policyToCompare.getRic());
   }

    private PolicyInstance getPolicyToCompare() {
        Scope scope = new Scope("ue_1");
        Resources resources = new Resources(Lists.newArrayList("Cell1"), Preference.AVOID);
        return PolicyInstance.builder()
                .json(new A1PolicyEvent(scope, Lists.newArrayList(resources)))
                .service("sleepingcelldetector")
                .ric("ric1")
                .build();
    }

    private RicConfiguration getRicConfig() {
        return new RicConfiguration("ric1", Collections.emptyList(), List.of("1000"), "AVAILABLE");
    }

}
