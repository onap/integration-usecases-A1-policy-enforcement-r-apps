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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.onap.rapp.datacollector.entity.pm.AggregatedPM;
import org.onap.rapp.datacollector.service.PMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Controller("pmController")
@Api(tags = {"RESTful APIs for DataCollector (current is PM DataCollector) R-APP mS"})
public class PMController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PMService pmService;

    public PMController(PMService pmService) {
        this.pmService = pmService;
    }

    @ApiOperation(value = "Get the latest aggregated pm ves events from database.",
            notes = "Returns the latest aggregated pm ves events from database between "
                    + "startTime and now, together with the itemsLength "
                    + "(i.e., total items in the returned pm array, i.e., active cells count)",
            httpMethod = "GET",
            produces = "application/json",
            response = AggregatedPM.class
    )
    @GetMapping(value = "/v1/pm/events/aggregatedmetrics")
    public @ResponseBody
    AggregatedPM retrievePMData(
            @ApiParam(value = "aggregation period (in seconds) for which an average performance "
                    + "metrics are calculated", required = true) @RequestParam("slot") int slot,
            @ApiParam(value = "number of aggregated performance metrics that should be returned by the method, "
                    + "one aggergated performance metric per each slot. The first performance metrics is avarage "
                    + "metrics for (startTime, startTime +slot)", required = true) @RequestParam("count") int count,
            @ApiParam(value = "ISO 8601 time format as string (e.g., 2020-10-26T06:52:54.01+00:00) for which aggregated "
                    + "performance metrics are calculated with the pm ves data starting from startTime. "
                    + "\"+\" and \".\" signs must be properly encoded in url",
                    required = true) @RequestParam("startTime") String startTime) {
        OffsetDateTime time = getOffsetDateTime(startTime);
        logger.debug("Getting {} aggregated metrics for {} second slot, start time {}", count, slot, startTime);

        return pmService.getAggregatedPMDataForTimeInterval(slot, count, time);
    }

    private OffsetDateTime getOffsetDateTime(String startTime) {
        OffsetDateTime time = OffsetDateTime.parse(startTime);
        if (time.toEpochSecond() > Instant.now().getEpochSecond()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time can't be from future.");
        }
        return time;
    }
}
