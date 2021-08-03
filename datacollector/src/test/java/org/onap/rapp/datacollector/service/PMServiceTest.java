package org.onap.rapp.datacollector.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.onap.rapp.datacollector.TestHelpers.getTestEventFromFile;
import static org.onap.rapp.datacollector.service.PMService.CELL_FIELD_NAME;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.rapp.datacollector.entity.pm.AggregatedPM;
import org.onap.rapp.datacollector.entity.pm.PMData;
import org.onap.rapp.datacollector.entity.ves.AdditionalMeasurements;
import org.onap.rapp.datacollector.entity.ves.CommonEventHeader;
import org.onap.rapp.datacollector.entity.ves.Event;
import org.onap.rapp.datacollector.entity.ves.EventAPI;
import org.onap.rapp.datacollector.entity.ves.MeasurementFields;

class PMServiceTest {

    public static final String TEST_CELL_ID = "Chn0000";

    PMService pmService;

    @Mock
    VesPersisterSqlImpl vesPersisterSql;

    @Mock
    DataAggregationService aggregationService;

    @Mock
    ParserFactory parser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        pmService = spy(new PMService(vesPersisterSql, aggregationService, parser));
    }

    @Test
    void getAggregatedPMDataForTimeInterval() {
        doReturn(getTestApiEvents()).when(vesPersisterSql).findEventsByTimeWindow(anyLong(), anyLong());
        doReturn(new PMData()).when(aggregationService).getAggregatedDataFromEventsForCell(any(), any(), any());
        doReturn(getTestEvents()).when(parser).getParsedEvents(any());
        AggregatedPM aggregatedPM = pmService.getAggregatedPMDataForTimeInterval(10, 10, OffsetDateTime.now());
        assertThat(aggregatedPM.getItemsLength()).isOne();
    }

    private List<EventAPI> getTestApiEvents() {
        return asList(EventAPI.builder().id(1L).rawdata(getTestEventFromFile("/sample-fileready.json")).build(),
                EventAPI.builder().id(2L).rawdata(getTestEventFromFile("/sample-fileready.json")).build());
    }

    protected List<Event> getTestEvents() {
        return asList(Event.of(CommonEventHeader.builder()
                        .build(), MeasurementFields.builder()
                        .additionalMeasurements(asList(AdditionalMeasurements.builder()
                                        .withEventId(1L)
                                        .withName(CELL_FIELD_NAME)
                                        .withHashMap(Map.of(CELL_FIELD_NAME, TEST_CELL_ID))
                                        .build(),
                                AdditionalMeasurements.builder()
                                        .withEventId(1L)
                                        .withName("latency")
                                        .withHashMap(Map.of("latency", "35"))
                                        .build())
                        )
                        .build()),
                Event.of(CommonEventHeader.builder()
                        .build(), MeasurementFields.builder()
                        .additionalMeasurements(asList(AdditionalMeasurements.builder()
                                        .withEventId(1L)
                                        .withName(CELL_FIELD_NAME)
                                        .withHashMap(Map.of(CELL_FIELD_NAME, TEST_CELL_ID))
                                        .build(),
                                AdditionalMeasurements.builder()
                                        .withEventId(1L)
                                        .withName("latency")
                                        .withHashMap(Map.of("latency", "59"))
                                        .build())
                        )
                        .build()));
    }
}