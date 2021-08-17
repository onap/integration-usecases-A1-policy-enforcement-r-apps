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


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.rapp.datacollector.entity.ves.EventTest;
import org.onap.rapp.datacollector.service.configuration.DmaapProperties;
import org.onap.rapp.datacollector.service.configuration.DmaapRestReaderConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class VesRetrievalServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DmaapRestReaderConfiguration config;

    @Mock
    private ParserFactory parser;

    @Mock
    private VesPersister persister;

    @Mock
    UEHolder ueHolder;

    private static final List<String> TOPIC_URLS = Collections.singletonList("http://localhost/a-topic");

    private VesRetrievalService service;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(config.getMeasurementsTopicUrls()).thenReturn(TOPIC_URLS);
        Mockito.when(config.getDmaapProperties()).thenReturn(getTestProperties());
        String[] response = new String[]{"a", "b"};

        Mockito.when(restTemplate.exchange(getTestTopicUrl(), HttpMethod.GET, new HttpEntity<>(createTestHeaders()), String[].class))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        service = new VesRetrievalService(restTemplate, parser, persister, config, ueHolder);
    }

    @Test
    void givenMockingIsDoneByMockRestServiceServer_whenGetIsCalled_thenReturnsMockedObject() {
        HashSet<String> actual = new HashSet<>(service.retrieveEvents());
        Set<String> expected = Set.of("a", "b");
        assertEquals(actual, expected);
    }

    @Test
    void whenGetIsCalled_thenExceptionIsThrown() {
        Mockito.when(restTemplate.exchange(getTestTopicUrl(), HttpMethod.GET, new HttpEntity<>(createTestHeaders()), String[].class))
                .thenThrow(new RestClientException("An test exception"));

        service = new VesRetrievalService(restTemplate, parser, persister, config, ueHolder);
        Collection<String> actual = service.retrieveEvents();
        assertEquals(0, actual.size());
    }

    @Test
    void whenRetrievedThenAlsoStored() {
        Mockito.when(parser.getParsedEvents(Mockito.any(String.class)))
                .thenReturn(EventTest.createDumyListOfEvents());

        service = new VesRetrievalService(restTemplate, parser, persister, config, ueHolder);
        service.retrieveAndStoreVesEvents();
        Mockito.verify(persister, Mockito.times(2)).persistAll(Mockito.any(List.class));
    }

    @Test
    void whenRetrievedThenAlsoStoredWithUE() {
        Mockito.when(parser.getParsedEvents(Mockito.any(String.class)))
                .thenReturn(EventTest.createDumyListOfEventsWithUe());

        UEHolder ueHolder = new UEHolder();

        service = new VesRetrievalService(restTemplate, parser, persister, config, ueHolder);
        service.retrieveAndStoreVesEvents();

        Mockito.verify(persister, Mockito.times(2)).persistAll(Mockito.any(List.class));
        assertEquals(ueHolder.getUes(), Set.of("emergency_samsung_01", "mobile_samsung_s10"));
    }


    private DmaapProperties getTestProperties() {
        DmaapProperties dmaapProperties = new DmaapProperties();
        dmaapProperties.setPassword("password");
        dmaapProperties.setUsername("user name");
        return dmaapProperties;
    }

    private HttpHeaders createTestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(getTestProperties().getUsername(), getTestProperties().getPassword());
        return headers;
    }

    private String getTestTopicUrl() {
        return TOPIC_URLS.get(0);
    }
}

