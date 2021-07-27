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
