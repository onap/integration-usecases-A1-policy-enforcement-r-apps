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


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.onap.rapp.sleepingcelldetector.configuration.SleepingCellDetectorConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.RicConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.ServiceRegistrationPayload;
import org.onap.rapp.sleepingcelldetector.entity.policy.PolicyInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PolicyAgentClient {

    private static final Logger logger = LoggerFactory.getLogger(PolicyAgentClient.class);

    public static final String SERVICE_KEEPALIVE_URL = "/services/keepalive?name=";
    public static final String SERVICE_URL = "/service";
    public static final String POLICY_TYPES_URL = "/policy_types";
    public static final String POLICY_URL = "/policy";
    public static final String POLICIES_URL = "/policies";
    public static final String SCD_SERVICE_NAME = "rapp-sleepingcelldetector";
    public static final String RICS_POLICY_TYPE_URL = "/rics?policyType=";
    public static final String POLICY_ACTOR_CALLBACK_URL = "http://rapp-sleepingcelldetector:8382/";

    private final SleepingCellDetectorConfiguration config;
    private final RestTemplate restTemplate;
    private final JsonHelper jsonHelper;

    public PolicyAgentClient(SleepingCellDetectorConfiguration config, RestTemplate restTemplate, JsonHelper jsonHelper) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.jsonHelper = jsonHelper;
    }

    public void sendPolicyEvent(PolicyInstance policy, RicConfiguration ricConfig) {
        ricConfig.getPolicyTypes().forEach(policyType -> {
            String policyServiceUrl = getUpdatePolicyUrl(ricConfig, policyType, policy.getId());
            String policyRequest = jsonHelper.objectToJsonString(policy.getJson());
            sendUpdatePolicyRequest(policyServiceUrl, policyRequest);
        });
    }

    private String getUpdatePolicyUrl(RicConfiguration ricConfig, String policyTypeId, String policyId) {
        String queryParams = POLICY_URL + "?id=" + policyId + "&ric=" + ricConfig.getRicName()
                + "&service=" + SCD_SERVICE_NAME + "&type=" + policyTypeId;
        return getA1PolicyBaseUrl() + queryParams;
    }

    private void sendUpdatePolicyRequest(String policyServiceUrl, String policyRequest) {
        logger.info("Sending policy event; URL: {},\n Policy: {}", policyServiceUrl, policyRequest);
        restTemplate.put(policyServiceUrl, createPolicyUpdateRequestEntity(policyRequest));
    }

    private HttpEntity<String> createPolicyUpdateRequestEntity(String policy) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(policy, headers);
    }

    public void deletePolicyInstance(String id){
        try {
            logger.info("Policy instance {} remove request will be send", id);
            String deletePolicyUrl = getA1PolicyBaseUrl() + POLICY_URL + "?id=" + id;
            restTemplate.delete(deletePolicyUrl);
        } catch (Exception e){
            logger.warn("Exception during policy deletion: {} \nPolicy {} was already removed", e.getMessage(), id);
        }

    }

    public List<String> getPoliciesIds() {
        String policyIdsUrl = getA1PolicyBaseUrl() + POLICY_TYPES_URL;
        ResponseEntity<String[]> policyIds = restTemplate.getForEntity(policyIdsUrl, String[].class);
        return Arrays.asList(Objects.requireNonNull(policyIds.getBody()));
    }

    public List<PolicyInstance> getPoliciesInstances(){
        String policiesUrl = getA1PolicyBaseUrl() + POLICIES_URL;
        ResponseEntity<PolicyInstance[]> policiesResponse = restTemplate.getForEntity(policiesUrl, PolicyInstance[].class);

        if (policiesResponse.hasBody()){
            return Arrays.asList(policiesResponse.getBody());
        } else {
            return Collections.emptyList();
        }
    }

    public List<RicConfiguration> getRicConfigurationsByPolicyId(String policyId) {
        String ricConfigUrl = getA1PolicyBaseUrl() + RICS_POLICY_TYPE_URL + policyId;
        ResponseEntity<RicConfiguration[]> ricConfigResponse = restTemplate.getForEntity(ricConfigUrl, RicConfiguration[].class);

        if (ricConfigResponse.hasBody()){
            return Arrays.asList(ricConfigResponse.getBody());
        } else {
            return Collections.emptyList();
        }
    }

    public boolean createService() {
        String createServiceUrl = getA1PolicyBaseUrl() + SERVICE_URL;
        ServiceRegistrationPayload payload = buildPayload();
        HttpEntity<String> entity = prepareRequest(payload);
        ResponseEntity<String> response = restTemplate.exchange(createServiceUrl, HttpMethod.PUT, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Service created");
            return true;
        } else {
            logger.warn("Problem with service registration request, response: {}", response.getStatusCode());
            return false;
        }
    }

    private HttpEntity<String> prepareRequest(ServiceRegistrationPayload payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(jsonHelper.objectToJsonString(payload), headers);
    }

    private ServiceRegistrationPayload buildPayload() {
        return ServiceRegistrationPayload.builder()
                .callbackUrl(POLICY_ACTOR_CALLBACK_URL)
                .keepAliveIntervalSeconds("20")
                .serviceName(SCD_SERVICE_NAME)
                .build();
    }

    public void sendKeepAliveRequest() {
        String createServiceUrl = getA1PolicyBaseUrl() + SERVICE_KEEPALIVE_URL + SCD_SERVICE_NAME;
        restTemplate.put(createServiceUrl, Void.class);
        logger.info("Keep alive request performed");
    }

    private String getA1PolicyBaseUrl() {
        return config.getA1PolicyBaseUrl();
    }


}
