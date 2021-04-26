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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.onap.rapp.sleepingcelldetector.configuration.SleepingCellDetectorConfiguration;
import org.onap.rapp.sleepingcelldetector.entity.pm.PMEntity;
import org.onap.rapp.sleepingcelldetector.entity.ue.UEInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DataCollectorClient {

    public static final String AGGREGATED_METRICS_URL = "/pm/events/aggregatedmetrics";
    public static final String UES_URL = "/pm/ues";
    private final SleepingCellDetectorConfiguration config;
    private final RestTemplate restTemplate;

    public DataCollectorClient(SleepingCellDetectorConfiguration config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    public PMEntity getPMData() throws UnsupportedEncodingException {
        String dataCollectorUrl = config.getDataCollectorBaseUrl() + AGGREGATED_METRICS_URL + getQueryParams();
        URI uri = URI.create(dataCollectorUrl);
        ResponseEntity<PMEntity> pmEntityResponse = restTemplate.getForEntity(uri, PMEntity.class);
        return pmEntityResponse.getBody();
    }

    private String getQueryParams() throws UnsupportedEncodingException {
        long slot = config.getPredictionTimeSlot();
        long count = config.getPredictionSlotNumber();
        String time = URLEncoder.encode(OffsetDateTime.now().minusSeconds(slot * count).toString(),
                StandardCharsets.UTF_8.toString());
        return new StringBuffer().append("?slot=").append(slot)
                .append("&count=").append(count)
                .append("&startTime=").append(time)
                .toString();
    }

    public UEInfo getUserEquipment() {
        String dataCollectorUrl = config.getDataCollectorBaseUrl() + UES_URL;
        ResponseEntity<UEInfo> userEquipmentResponse = restTemplate.getForEntity(dataCollectorUrl, UEInfo.class);
        return userEquipmentResponse.getBody();
    }
}
