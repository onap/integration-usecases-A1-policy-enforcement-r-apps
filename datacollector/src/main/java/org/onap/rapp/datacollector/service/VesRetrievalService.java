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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.stream.Stream;
import org.onap.rapp.datacollector.entity.ves.AdditionalMeasurementValues;
import org.onap.rapp.datacollector.entity.ves.AdditionalMeasurements;
import org.onap.rapp.datacollector.entity.ves.Event;
import org.onap.rapp.datacollector.service.configuration.DmaapRestReaderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class VesRetrievalService implements DmaapRestReader {

    private static final Logger logger = LoggerFactory.getLogger(VesRetrievalService.class);
    public static final String UE_FIELD_NAME = "trafficModel";

    private final RestTemplate restTemplate;
    private final DmaapRestReaderConfiguration config;
    private final ParserFactory parser;
    private final VesPersister persister;
    private final UEHolder ueHolder;

    @Autowired
    public VesRetrievalService(RestTemplate restTemplate, ParserFactory parser, VesPersister persister,
            DmaapRestReaderConfiguration configuration, UEHolder ueHolder) {
        this.restTemplate = restTemplate;
        this.parser = parser;
        this.persister = persister;
        this.config = configuration;
        this.ueHolder = ueHolder;
    }

    @Override
    public Collection<String> retrieveEvents() {
        logger.info("Reaching from dmaap: {}", config.getMeasurementsTopicUrls());
        return config.getMeasurementsTopicUrls().stream().flatMap(this::retrieveEventsFromTopic)
                .collect(Collectors.toList());
    }

    private Stream<String> retrieveEventsFromTopic(String topic) {
        try {
            ResponseEntity<String[]> responseEntity = restTemplate.exchange(topic, HttpMethod.GET,
                    new HttpEntity<String>(createHeaders(config.getDmaapProperties().getUsername(),
                            config.getDmaapProperties().getPassword())), String[].class);
            if (responseEntity.hasBody()) {
                String[] events = responseEntity.getBody();
                return Arrays.stream(events);
            }
        } catch (RestClientException ex) {
            logger.error("Failed to reach to dmaap", ex);
        }

        return Arrays.stream(new String[0]);
    }

    private HttpHeaders createHeaders(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        return headers;
    }

    @Scheduled(fixedRate = 5000)
    public void retrieveAndStoreVesEvents() {
        retrieveEvents().stream().map(parser::getParsedEvents).forEach(this::saveAllEvents);
    }

    private void saveAllEvents(List<Event> events) {
        persister.persistAll(events);
        saveUesOfVes(events);
    }

    private void saveUesOfVes(List<Event> events) {
        Set<String> uesOfVes = Optional.ofNullable(events).orElse(Collections.emptyList()).stream().flatMap(event -> getUserEquipmentData(event).stream())
                .collect(Collectors.toSet());
        uesOfVes.forEach(ueHolder::addUE);
    }

    private Set<String> getUserEquipmentData(Event event) {
        Optional<AdditionalMeasurements> ues = event.getMeasurementFields().getAdditionalMeasurements()
                .stream().filter(am -> am.getName().equals(UE_FIELD_NAME)).findAny();
        return ues.map(additionalMeasurements -> additionalMeasurements.getValues().stream()
                .map(AdditionalMeasurementValues::getParameterName)
                .collect(Collectors.toSet())).orElse(Collections.emptySet());
    }

}



