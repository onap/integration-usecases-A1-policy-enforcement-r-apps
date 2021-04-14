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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.onap.rapp.sleepingcelldetector.entity.RicConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.policy.PolicyInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PolicyInstanceManager {

    private static final Logger logger = LoggerFactory.getLogger(PolicyInstanceManager.class);
    private Multimap<String, PolicyInstance> cellPolicyInstancesMap = ArrayListMultimap.create();

    private final PolicyAgentClient policyAgentClient;
    private final PolicyInstanceBuilder payloadBuilder;

    public PolicyInstanceManager(PolicyAgentClient policyAgentClient, PolicyInstanceBuilder payloadBuilder) {
        this.policyAgentClient = policyAgentClient;
        this.payloadBuilder = payloadBuilder;
    }

    public boolean cellContainsPolicy(String cell) {
        return cellPolicyInstancesMap.containsKey(cell);
    }

    public boolean cellContainsPolicyForUe(String cell, String ue) {
        if (cellContainsPolicy(cell)) {
            List<PolicyInstance> policyInstances = new ArrayList<>(cellPolicyInstancesMap.get(cell));
            return policyInstances.stream().anyMatch(pi -> pi.getJson().getScope().getUeId().equals(ue));
        }
        return false;
    }

    public void createPolicyInstance(String cell, String ue, RicConfiguration ric) {
        try {
            PolicyInstance policy = payloadBuilder.buildPolicyInstance(cell, ue, ric);
            cellPolicyInstancesMap.put(cell, policy);
            policyAgentClient.sendPolicyEvent(policy, ric);
            logger.info("Policy Instance for ue {}, cell {} created with id {}", ue, cell, policy.getId());
        } catch (Exception e) {
            logger.error("Error during request to Policy Management Service: {}", e.getMessage());
            removePolicyInstancesForCell(cell);
        }
    }

    public void addPolicyInstance(PolicyInstance policyInstance) {
        List<String> cells = policyInstance.getJson().getResources()
                .stream().flatMap(resources -> resources.getCellIdList().stream()).collect(Collectors.toList());
        cells.forEach(cell -> cellPolicyInstancesMap.put(cell, policyInstance));
    }

    public void removePolicyInstancesForCell(String cell) {
        Collection<PolicyInstance> policyInstancesByCell = cellPolicyInstancesMap.get(cell);
        policyInstancesByCell.forEach(pi -> policyAgentClient.deletePolicyInstance(pi.getId()));
        cellPolicyInstancesMap.removeAll(cell);
        logger.info("Policy Instances for cell {} removed", cell);
    }

}
