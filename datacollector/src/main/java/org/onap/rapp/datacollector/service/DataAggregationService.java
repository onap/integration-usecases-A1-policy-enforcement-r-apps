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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import org.onap.rapp.datacollector.entity.DataAggregationInfo;
import org.onap.rapp.datacollector.entity.pm.PMData;
import org.onap.rapp.datacollector.entity.pm.PerformanceData;
import org.onap.rapp.datacollector.entity.ves.AdditionalMeasurements;
import org.onap.rapp.datacollector.entity.ves.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DataAggregationService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String LATENCY_FIELD_NAME = "latency";
    private static final String THROUGHPUT_FIELD_NAME = "throughput";
    public static final int MEASUREMENT_INDEX = 0;

    public PMData getAggregatedDataFromEventsForCell(String cellId, List<Event> events, DataAggregationInfo dataAggregationInfo) {
        logger.info("Cell {}, events size {}", cellId, events.size());
        Collection<List<Event>> eventsByTime = groupEventsTimeSlots(events, dataAggregationInfo);

        List<PerformanceData> pmDataList = new ArrayList<>();
        eventsByTime.forEach(slotOfEvents -> {
            List<Integer> latencyList = getPerformanceData(slotOfEvents, LATENCY_FIELD_NAME);
            List<Integer> throughputList = getPerformanceData(slotOfEvents, THROUGHPUT_FIELD_NAME);

            Integer latencyAggregatedData = getAverage(latencyList);
            Integer throughputAggregatedData = getAverage(throughputList);

            PerformanceData pm = new PerformanceData(latencyAggregatedData, throughputAggregatedData);
            pmDataList.add(pm);
        });

        return new PMData(cellId, pmDataList);
    }

    private Collection<List<Event>> groupEventsTimeSlots(List<Event> events, DataAggregationInfo aggregationInfo) {
        long slotStartTime = aggregationInfo.getStartTime();
        long slotEndTime = slotStartTime + aggregationInfo.getSlot();

        List<List<Event>> eventsByTime = new ArrayList<>();
        List<Event> eventsOfSlot = new ArrayList<>();

        for (Event event : events) {
            if (isInNextSlot(slotEndTime, event)) {
                eventsByTime.add(eventsOfSlot);
                eventsOfSlot = new ArrayList<>();

                slotStartTime = slotEndTime;
                slotEndTime = slotStartTime + aggregationInfo.getSlot();

                while (isInNextSlot(slotEndTime, event)){
                    eventsByTime.add(Collections.emptyList());
                    slotStartTime = slotEndTime;
                    slotEndTime = slotStartTime + aggregationInfo.getSlot();
                }
            }
            eventsOfSlot.add(event);
        }

        eventsByTime.add(eventsOfSlot);
        fillEmptyEndIfNeeded(aggregationInfo, slotEndTime, eventsByTime);
        return eventsByTime;
    }

    private boolean isInNextSlot(long slotEndTime, Event event) {
        return event.getCommonEventHeader().getLastEpochMicrosec() > slotEndTime;
    }

    private void fillEmptyEndIfNeeded(DataAggregationInfo aggregationInfo, long slotEndTime, List<List<Event>> eventsByTime) {
        while (slotEndTime < aggregationInfo.getEndTime()){
            eventsByTime.add(Collections.emptyList());
            slotEndTime = slotEndTime + aggregationInfo.getSlot();
        }
    }

    private List<Integer> getPerformanceData(List<Event> events, String measurement) {
        return events.stream().map(e -> getPerformanceDataFromEvent(e, measurement))
                .collect(Collectors.toList());
    }

    private int getPerformanceDataFromEvent(Event event, String name) {
        AdditionalMeasurements performance = event.getMeasurementFields().getAdditionalMeasurements()
                .stream().filter(am -> am.getName().equals(name)).findAny().orElseThrow();
        return Integer.parseInt(performance.getValues().get(MEASUREMENT_INDEX).getParameterValue());
    }

    private Integer getAverage(List<Integer> values) {
        if (values.isEmpty()) {
            return null;
        }
        OptionalDouble average = values.stream().mapToDouble(l -> l).average();

        if (average.isPresent()) {
            return (int) average.getAsDouble();
        } else {
            return null;
        }
    }

}
