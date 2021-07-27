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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.onap.rapp.datacollector.entity.DataAggregationInfo;
import org.onap.rapp.datacollector.entity.pm.AggregatedPM;
import org.onap.rapp.datacollector.entity.pm.PMData;
import org.onap.rapp.datacollector.entity.ves.AdditionalMeasurements;
import org.onap.rapp.datacollector.entity.ves.Event;
import org.onap.rapp.datacollector.entity.ves.EventAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("pmService")
public class PMService {

    private static final Logger logger = LoggerFactory.getLogger(PMService.class);

    public static final String CELL_FIELD_NAME = "identifier";
    public static final String VALUE_NAME = "value";
    public static final int CELL_INDEX = 0;
    private static final int MICRO_SECONDS_OF_SECOND = 1_000_000;

    private final VesPersisterSqlImpl vesPersisterSql;
    private final DataAggregationService aggregationService;
    private final ParserFactory parser;

    public PMService(VesPersisterSqlImpl vesPersisterSql, DataAggregationService aggregationService, ParserFactory parser) {
        this.vesPersisterSql = vesPersisterSql;
        this.aggregationService = aggregationService;
        this.parser = parser;
    }

    public AggregatedPM getAggregatedPMDataForTimeInterval(int slot, int count, OffsetDateTime startTime) {
        DataAggregationInfo aggregationInfo = buildDataAggregationInfo(slot, count, startTime);
        logger.info("Start Time: {}, EndTime: {}", aggregationInfo.getStartTime(), aggregationInfo.getEndTime());
        List<EventAPI> eventsOfInterval = vesPersisterSql.findEventsByTimeWindow(aggregationInfo.getStartTime(), aggregationInfo.getEndTime());
        Map<String, List<Event>> eventsByCell = groupByCell(eventsOfInterval);
        List<PMData> pmDataList = calculateAggregatedData(aggregationInfo, eventsByCell);
        return new AggregatedPM(pmDataList, pmDataList.size());
    }

    private DataAggregationInfo buildDataAggregationInfo(int slot, int count, OffsetDateTime startTime) {
        long timeIntervalStartTime = startTime.toEpochSecond() * MICRO_SECONDS_OF_SECOND;
        long timeIntervalEndTime = getTimeIntervalEndTime(slot, count, timeIntervalStartTime);
        return DataAggregationInfo.builder()
                .startTime(timeIntervalStartTime)
                .endTime(timeIntervalEndTime)
                .slot(slot * MICRO_SECONDS_OF_SECOND)
                .build();
    }

    private long getTimeIntervalEndTime(int slot, int count, long startDate) {
        int timeIntervalMicrosec = slot * count * MICRO_SECONDS_OF_SECOND;
        return startDate + timeIntervalMicrosec;
    }

    private Map<String, List<Event>> groupByCell(List<EventAPI> events) {
        return events.stream().flatMap(e -> parser.getParsedEvents(e.getRawdata()).stream())
                .collect(Collectors.groupingBy(this::getCellFromVes));
    }

    private String getCellFromVes(Event event) {
        AdditionalMeasurements cellField = event.getMeasurementFields().getAdditionalMeasurements()
                .stream().filter(am -> am.getName().equals(CELL_FIELD_NAME)).findAny().orElseThrow();
        return cellField.getValues().get(CELL_INDEX).getParameterValue();
    }

    private List<PMData> calculateAggregatedData(DataAggregationInfo dataAggregationInfo, Map<String, List<Event>> events) {
        return events.entrySet().stream()
                .map(e -> aggregationService.getAggregatedDataFromEventsForCell(e.getKey(), e.getValue(), dataAggregationInfo))
                .collect(Collectors.toList());
    }

}
