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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.rapp.datacollector.entity.ves.EventTest;
import org.onap.rapp.datacollector.service.configuration.DmaapRestReaderConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class VesRetrievalServiceTest {

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

    private VesRetrievalService service;

    @Before
    public void init() {
        Mockito.when(config.getMeasurementsTopicUrl()).thenReturn("http://localhost/a-topic");
        String[] response = new String[]{"a", "b"};
        Mockito.when(restTemplate.getForEntity("http://localhost/a-topic", String[].class))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        service = new VesRetrievalService(restTemplate, parser, persister, config, ueHolder);
    }

    @Test
    public void givenMockingIsDoneByMockRestServiceServer_whenGetIsCalled_thenReturnsMockedObject() {
        HashSet<String> actual = new HashSet<>(service.retrieveEvents());
        Set<String> expected = Set.of("a", "b");
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void whenGetIsCalled_thenExceptionIsThrown() {
        Mockito.when(config.getMeasurementsTopicUrl()).thenReturn("http://localhost/a-topic");
        Mockito.when(restTemplate.getForEntity("http://localhost/a-topic", String[].class))
                .thenThrow(new RestClientException("An test exception"));

        service = new VesRetrievalService(restTemplate, parser, persister, config, ueHolder);
        Collection<String> actual = service.retrieveEvents();
        Assert.assertEquals(0, actual.size());
    }

    @Test
    public void whenRetrievedThenAlsoStored() {
        Mockito.when(config.getMeasurementsTopicUrl()).thenReturn("http://localhost/a-topic");
        Mockito.when(restTemplate.getForEntity("http://localhost/a-topic", String[].class))
                .thenReturn(new ResponseEntity<>(new String[]{"dead", "beef"}, HttpStatus.OK));
        Mockito.when(parser.getParsedEvents(Mockito.any(String.class)))
                .thenReturn(EventTest.createDumyListOfEvents());

        service = new VesRetrievalService(restTemplate, parser, persister, config, ueHolder);
        service.retrieveAndStoreVesEvents();

        Mockito.verify(persister, Mockito.times(2)).persistAll(Mockito.any(List.class));
    }

    @Test
    public void whenRetrievedThenAlsoStoredWithUE() {
        Mockito.when(config.getMeasurementsTopicUrl()).thenReturn("http://localhost/a-topic");
        Mockito.when(restTemplate.getForEntity("http://localhost/a-topic", String[].class))
                .thenReturn(new ResponseEntity<>(new String[]{"dead", "beef"}, HttpStatus.OK));
        Mockito.when(parser.getParsedEvents(Mockito.any(String.class)))
                .thenReturn(EventTest.createDumyListOfEventsWithUe());

        UEHolder ueHolder = new UEHolder();

        service = new VesRetrievalService(restTemplate, parser, persister, config, ueHolder);
        service.retrieveAndStoreVesEvents();

        Mockito.verify(persister, Mockito.times(2)).persistAll(Mockito.any(List.class));
        Assert.assertEquals(ueHolder.getUes(), Set.of("emergency_samsung_01", "mobile_samsung_s10"));
    }
}

