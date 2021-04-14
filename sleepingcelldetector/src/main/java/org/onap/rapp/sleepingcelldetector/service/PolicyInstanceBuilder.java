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
import java.util.UUID;
import org.onap.rapp.sleepingcelldetector.entity.RicConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.policy.A1PolicyEvent;
import org.onap.rapp.sleepingcelldetector.entity.policy.PolicyInstance;
import org.onap.rapp.sleepingcelldetector.entity.policy.Preference;
import org.onap.rapp.sleepingcelldetector.entity.policy.Resources;
import org.onap.rapp.sleepingcelldetector.entity.policy.Scope;
import org.springframework.stereotype.Service;

@Service
public class PolicyInstanceBuilder {

    public static final String SCD_SERVICE_NAME = "sleepingcelldetector";

    public PolicyInstance buildPolicyInstance(String cell, String userEquipment, RicConfiguration ric) {
        Scope scope = new Scope(userEquipment);
        Resources resources = new Resources(Lists.newArrayList(cell), Preference.AVOID);
        String id = UUID.randomUUID().toString();
        return PolicyInstance.builder()
                .id(id)
                .ric(ric.getRicName())
                .service(SCD_SERVICE_NAME)
                .json(new A1PolicyEvent(scope, Lists.newArrayList(resources)))
                .build();
    }

}
