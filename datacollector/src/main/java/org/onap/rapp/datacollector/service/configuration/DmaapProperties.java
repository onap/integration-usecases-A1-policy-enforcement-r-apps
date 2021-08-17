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

package org.onap.rapp.datacollector.service.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "dmaap")
public class DmaapProperties {

    private String protocol;
    private String host;
    private String username;
    private String password;
    private int port;
    private List<String> measurementsTopics = new ArrayList<>();

    public List<String> getMeasurementsTopicUrls() {
        return measurementsTopics.stream().map(topic -> String.format("%s://%s:%d/%s", protocol, host, port, topic))
                .collect(Collectors.toList());
    }

}
