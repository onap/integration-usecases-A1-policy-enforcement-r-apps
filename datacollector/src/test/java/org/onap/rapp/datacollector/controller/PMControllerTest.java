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

package org.onap.rapp.datacollector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.rapp.datacollector.entity.pm.AggregatedPM;
import org.onap.rapp.datacollector.entity.pm.PMData;
import org.onap.rapp.datacollector.service.PMService;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(PMController.class)
@ActiveProfiles("test")
public class PMControllerTest {

    private static final int SLOT = 10;
    private static final int COUNT = 12;
    private static final String startTime = OffsetDateTime.now().minusSeconds(SLOT * COUNT).toString();

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "pmService")
    private PMService pmService;

    @Autowired
    private ObjectMapper mapper;

    protected <T> T mapFromJson(String json, Class<T> clazz) throws IOException {
        return mapper.readValue(json, clazz);
    }

    private List<PMData> pmDataList;

    @Before
    public void setUp() throws Exception {
        String testPmContent = getSamplePMData();
        pmDataList = Collections.singletonList(this.mapFromJson(testPmContent, PMData.class));
    }

    @Test
    public void retrievePMData() throws Exception {
        when(pmService.getAggregatedPMDataForTimeInterval(SLOT, COUNT, OffsetDateTime.parse(startTime))).thenReturn(new AggregatedPM(pmDataList, pmDataList.size()));

        BDDMockito
                .given(pmService.getAggregatedPMDataForTimeInterval(SLOT, COUNT, OffsetDateTime.parse(startTime)))
                .willReturn(new AggregatedPM(pmDataList, pmDataList.size()));

        mockMvc
                // when
                .perform(
                        MockMvcRequestBuilders
                                .get("/v1/pm/events/aggregatedmetrics")
                                .param("slot", String.valueOf(SLOT))
                                .param("count", String.valueOf(COUNT))
                                .param("startTime", startTime)
                                .accept(MediaType.APPLICATION_JSON)
                )
                // then
                .andDo(MockMvcResultHandlers.print())
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                )
                .andExpect(
                        MockMvcResultMatchers
                                .content()
                                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue())
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].cellId", Matchers.is("Cell1"))
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.itemsLength", Matchers.is(1))
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].performance[0].latency", Matchers.is(20))
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].performance[0].throughput", Matchers.is(80))
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].performance.*", hasSize(12))
                )
        ;

        // verify
        BDDMockito
                .verify(pmService, VerificationModeFactory.times(1))
                .getAggregatedPMDataForTimeInterval(SLOT, COUNT, OffsetDateTime.parse(startTime));
    }

    @Test
    public void retrievePMDataWithEmptySlotOnBeginning() throws Exception {
        pmDataList.get(0).getPerformance().get(0).setLatency(null);
        pmDataList.get(0).getPerformance().get(0).setThroughput(null);

        when(pmService.getAggregatedPMDataForTimeInterval(SLOT, COUNT, OffsetDateTime.parse(startTime))).thenReturn(new AggregatedPM(pmDataList, pmDataList.size()));

        BDDMockito
                .given(pmService.getAggregatedPMDataForTimeInterval(SLOT, COUNT, OffsetDateTime.parse(startTime)))
                .willReturn(new AggregatedPM(pmDataList, pmDataList.size()));

        mockMvc
                // when
                .perform(
                        MockMvcRequestBuilders
                                .get("/v1/pm/events/aggregatedmetrics")
                                .param("slot", String.valueOf(SLOT))
                                .param("count", String.valueOf(COUNT))
                                .param("startTime", startTime)
                                .accept(MediaType.APPLICATION_JSON)
                )
                // then
                .andDo(MockMvcResultHandlers.print())
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                )
                .andExpect(
                        MockMvcResultMatchers
                                .content()
                                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].cellId", Matchers.is("Cell1"))
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.itemsLength", Matchers.is(1))
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].performance[0].latency").value(IsNull.nullValue())
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].performance[0].throughput").value(IsNull.nullValue())
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].performance.*", hasSize(12))
                )
        ;

        // verify
        BDDMockito
                .verify(pmService, VerificationModeFactory.times(1))
                .getAggregatedPMDataForTimeInterval(SLOT, COUNT, OffsetDateTime.parse(startTime));
    }

    @Test
    public void retrievePMDataWithEmptySlotOnEnd() throws Exception {
        pmDataList.get(0).getPerformance().get(11).setLatency(null);
        pmDataList.get(0).getPerformance().get(11).setThroughput(null);

        when(pmService.getAggregatedPMDataForTimeInterval(SLOT, COUNT, OffsetDateTime.parse(startTime))).thenReturn(new AggregatedPM(pmDataList, pmDataList.size()));

        BDDMockito
                .given(pmService.getAggregatedPMDataForTimeInterval(SLOT, COUNT, OffsetDateTime.parse(startTime)))
                .willReturn(new AggregatedPM(pmDataList, pmDataList.size()));

        mockMvc
                // when
                .perform(
                        MockMvcRequestBuilders
                                .get("/v1/pm/events/aggregatedmetrics")
                                .param("slot", String.valueOf(SLOT))
                                .param("count", String.valueOf(COUNT))
                                .param("startTime", startTime)
                                .accept(MediaType.APPLICATION_JSON)
                )
                // then
                .andDo(MockMvcResultHandlers.print())
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                )
                .andExpect(
                        MockMvcResultMatchers
                                .content()
                                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].cellId", Matchers.is("Cell1"))
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.itemsLength", Matchers.is(1))
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].performance[11].latency").value(IsNull.nullValue())
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].performance[11].throughput").value(IsNull.nullValue())
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].performance.*", hasSize(12))
                )
        ;

        // verify
        BDDMockito
                .verify(pmService, VerificationModeFactory.times(1))
                .getAggregatedPMDataForTimeInterval(SLOT, COUNT, OffsetDateTime.parse(startTime));
    }

    @Test
    public void retrievePMDataWithEmptySlotInMiddle() throws Exception {
        pmDataList.get(0).getPerformance().get(5).setLatency(null);
        pmDataList.get(0).getPerformance().get(5).setThroughput(null);

        when(pmService.getAggregatedPMDataForTimeInterval(SLOT, COUNT, OffsetDateTime.parse(startTime))).thenReturn(new AggregatedPM(pmDataList, pmDataList.size()));

        BDDMockito
                .given(pmService.getAggregatedPMDataForTimeInterval(SLOT, COUNT, OffsetDateTime.parse(startTime)))
                .willReturn(new AggregatedPM(pmDataList, pmDataList.size()));

        mockMvc
                // when
                .perform(
                        MockMvcRequestBuilders
                                .get("/v1/pm/events/aggregatedmetrics")
                                .param("slot", String.valueOf(SLOT))
                                .param("count", String.valueOf(COUNT))
                                .param("startTime", startTime)
                                .accept(MediaType.APPLICATION_JSON)
                )
                // then
                .andDo(MockMvcResultHandlers.print())
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                )
                .andExpect(
                        MockMvcResultMatchers
                                .content()
                                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].cellId", Matchers.is("Cell1"))
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.itemsLength", Matchers.is(1))
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].performance[5].latency").value(IsNull.nullValue())
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].performance[5].throughput").value(IsNull.nullValue())
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.pm[0].performance.*", hasSize(12))
                )
        ;

        // verify
        BDDMockito
                .verify(pmService, VerificationModeFactory.times(1))
                .getAggregatedPMDataForTimeInterval(SLOT, COUNT, OffsetDateTime.parse(startTime));
    }

    private String getSamplePMData() throws Exception {
        String testPmContent;
        InputStream in = this.getClass().getResourceAsStream("/sample-pm.json");
        try (in) {
            BufferedReader inr = new BufferedReader(new InputStreamReader(in));
            testPmContent = inr.lines().collect(Collectors.joining(" "));
        }
        return testPmContent;
    }
}