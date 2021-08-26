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

package org.onap.rapp.datacollector.entity.fileready;

import java.util.List;

import lombok.Getter;

/**
 * MeasDataCollection section of PM Bulk File coming from PM Mapper
 */
@Getter
public class MeasDataCollection {

    private long granularityPeriod;
    private String measuredEntityUserName;
    private String measuredEntityDn;
    private String measuredEntitySoftwareVersion;

    private List<MeasInfo> measInfoList;

    @Getter
    public class MeasInfo {

        private MeasInfoId measInfoId;

        @Getter
        public class MeasInfoId {

            private String sMeasInfoId;
        }

        private MeasTypes measTypes;

        @Getter
        public class MeasTypes {

            List<String> sMeasTypesList;
        }

        private List<MeasValue> measValuesList;

        @Getter
        public class MeasValue {

            private String measObjInstId;
            private boolean suspectFlag;
            private List<MeasResult> measResults;

            @Getter
            public class MeasResult {

                private Integer p;
                private String sValue;

            }
        }

    }
}
