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
import java.util.Set;
import org.onap.rapp.datacollector.entity.UEInfo;
import org.onap.rapp.datacollector.service.UEHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Api(tags = {"RESTful APIs for DataCollector (current is PM DataCollector) R-APP mS"})
public class UEController {

    private final UEHolder holder;

    public UEController(UEHolder holder) {
        this.holder = holder;
    }

    @ApiOperation(value = "Get all user equipment from topology.",
            notes = "Returns all user equipment from topology.",
            httpMethod = "GET",
            produces = "application/json",
            response = UEInfo.class
    )
    @GetMapping(value = "/v1/pm/ues")
    public @ResponseBody
    UEInfo getUserEquipments() {
        Set<String> ues = holder.getUes();
        return new UEInfo(ues);
    }

}
