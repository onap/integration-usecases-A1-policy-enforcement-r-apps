# SleepingCellDetector R-APP

SleepingCellDetector R-APP is a Java SpringBoot application for reading Aggregated PM Metrics from DataCollector R-APP
and make sleeping cell prediction based on PM data

## Use of SleepingCellDetector R-APP

SleepingCellDetector R-APP needs several parameters to be defined before start.

TCA Algorithm configuration situated in `resources/tca.json` file, example:

```json
[
  {
    "name": "latency",
    "condition": "MORE_OR_EQUAL",
    "averageThresholdValue": 400,
    "latestThresholdValue": 150,
    "latestSize": 2
  },
  {
    "name": "throughput",
    "condition": "LESS_OR_EQUAL",
    "averageThresholdValue": 10,
    "latestThresholdValue": 10,
    "latestSize": 2
  }
]
```

File contains information about names of performance measurement parameters, conditions and values of thresholds for
them.
`"averageThresholdValue"`- is a threshold of performance signal average
`"latestThresholdValue"` - is a threshold of last slots of performance signal (number defined in "
latestThresholdValue"). Needed to detect correction of performance signal. Conditions available: "LESS", "LESS_OR_EQUAL"
, "EQUAL", "MORE_OR_EQUAL", "MORE"'

Actually DataCollector R-APP returns Aggregated Metrics of "latency" and "throughput" parameters, example:

```json
{
  "pm": [
    {
      "cellId": "Cell1",
      "performance": [
        {
          "latency": 50,
          "throughput": 80
        },
        {
          "latency": 50,
          "throughput": 80
        },
        {
          "latency": 50,
          "throughput": 80
        }
      ]
    }
  ],
  "itemsLength": 1
}
```

Set parameters of environment variables:

A1PolicyManagementService URL can be set using environment variables:

- A1\_HOST
- A1\_PORT

To customize DataCollector R-APP connectivity you need to set the following:

- DC_HOST
- DC_PORT
- DC_VERSION

SleepingCellDetector R-APP configuration fields:

- SCD_PREFIX
- SCD_SLOT
- SCD_COUNT

Prefix of high priority user equipment (policy instances will be created only for user equipments with this prefix),
example:
`emergency_` - policy instances will be created only for UEs with "emergency\_" prefix Slot of time in seconds,
number of slots for DataCollector R-APP Aggregated Metrics endpoint call. Slot: aggregation period (in seconds) for
which an average performance metrics are calculated Count: number of aggregated performance metrics that should be
returned by the method, one aggregated performance metric per each slot. The first performance metrics is average
metrics for (startTime, startTime +slot). StartTime for DataCollector R-APP Aggregated Metrics endpoint call is
generated based on slot and count parameters as "time.now - slot\*count"

Example configuration in environment variables in application.yml:

```
server:
  port: 8382
a1:
  host: "policy-agent"
  port: 8081
dc:
  host: "localhost"
  port: 8087
  version: "v1"
scd:
  prefix: "emergency"
  slot: 10
  count: 12
logging:
  level:
    org:
      springframework: DEBUG
  logging.file.name: logs/rapp-sleepingcelldetector.log
  pattern:
    console: "%d %-5level %logger : %msg%n"
    file: "%d %-5level [%thread] %logger : %msg%n"
```

During start-up, SleepingCellDetector R-APP registers itself as a service in A1PolicyManagmentService(PMS). After that
SleepingCellDetector R-APP sends periodic keepalive requests to PMS. PMS exposes the Ric Configuration for SleepingCellDetector R-APP.
Ric Configuration contains information about Policy Type and Ric name, example:

```json
[
  {
    "ricName": "ric1",
    "managedElementIds": [
    ],
    "policyTypes": [
      "1000"
    ],
    "state": "AVAILABLE"
  }
]
```

SleepingCellDetector R-APP uses this information with created UUID are used for Policy Instances creation request to
A1PolicyManagementService, example:

`{a1policymanagementservicehost}/policy?id=123e4567-e89b-12d3-a456-426614174000&ric=ric1&service=rapp-sleepingcelldetector&type=1000`

```json
{
  "scope": {
    "ueId": "emergency_samsung_s10_01"
  },
  "resources": [
    {
      "cellIdList": [
        "Cell3"
      ],
      "preference": "AVOID"
    }
  ]
}
```

SleepingCellDetector R-APP creates the policy resources, which are used to traffic steering,
selecting cell for a connection, or scheduling traffic on available cells,
in a different way than what would be done through default behaviour.

Like was presented on the example above, created policy instances provided information
which cells (mention in the array "cellIdList") the User Equipment (define in the section "scope", key "ueId"")
should avoid (section "resources", key "preference").

After start-up SleepingCellDetector R-APP begins to make predictions periodically in intervals defined by CSD_SLOT
period based on new PM metrics measurement data received in each interval.
When sleeping cell is detected, creation of A1 Policy instance is enforced by SleepingCellDetector R-APP.
If cell becomes active again, A1Policy instance deletion request is sent.

# Developer Guide

## Build SleepingCellDetector R-APP

Following mvn command (in the current directory) will build SleepingCellDetector R-APP:

```bash
mvn clean install
```

To build docker image add `-P docker:build` flag.

## Run SleepingCellDetector R-APP

Following command will run SleepingCellDetector R-APP:

```bash
java -jar sleepingcelldetector-0.0.1-SNAPSHOT.jar org.onap.rapp.sleepingcelldetector.SleepingCellDetectorApplication
```

## Logging

The log file will be created in the /log path. Parameters of logging are in application.yml file.
After SleepingCellDetector R-APP starts successfully, log/rapp-sleepingcelldetector.log should start to contain the logs:

```
.
└──log
    └── rapp-sleepingcelldetector.log
```
