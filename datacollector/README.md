# DataCollector R-APP

DataCollector R-APP is a Java SpringBoot application for consuming PM VES data from DMaaP and store it into DB. Also
DataCollector R-APP provides API to query aggregated metrics from PM VES data and User Equipment info.

## Use of DataCollector R-APP

### Configuration

DataCollector R-APP needs several parameters to be defined before start. All parameters are passed through environment
variables

To customize DB connection you need to create database schema. To do this use `init.sql` script under **src/main/docker/**.
Additionally, you can use `start.sh` after you build DataCollector R-APP (please see **Build DataCollector R-APP** chapter). Those actions will create database schema and run the application.
Also, you will need to set the following environment variables:

- DATABASE_URL
- DATABASE_USERNAME
- DATABASE_PASSWORD

To customize VES connectivity you need to set the following:

- DMAAP_HOST
- DMAAP_POR
- DMAAP_MEASUREMENTS_TOPIC

Example configuration in environment variables in application.yml:

```
server:
  port: 8087
dmaap:
  prtocol: "http"
  host: "localhost"
  port: 8181
  measurements-topics: 
    - "measurements"
database:
  host: mariadb-host
  port: 3306
  name: "ves"
  username: ves
  driver-class-name: "org.mariadb.jdbc.Driver"
logging:
  level:
    org:
      springframework: DEBUG
  logging.file.name: logs/rapp-datacollector.log
  pattern:
    console: "%d %-5level %logger : %msg%n"
    file: "%d %-5level [%thread] %logger : %msg%n"
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
  main:
    allow-bean-definition-overriding: true

```

After startup DataCollector R-APP is ready to read VES Events from DMaaP and stores them in database if they are flowing
into DMaaP.

VES Event example

```json
{
  "event": {
    "commonEventHeader": {
      "version": "4.0.1",
      "vesEventListenerVersion": "7.0.1",
      "sourceId": "de305d54-75b4-431b-adb2-eb6b9e546014",
      "reportingEntityName": "ibcx0001vm002oam001",
      "startEpochMicrosec": 1603293000000000,
      "eventId": "measurement0000259",
      "lastEpochMicrosec": 1603292917149000,
      "priority": "Normal",
      "sequence": 3,
      "sourceName": "ibcx0001vm002ssc001",
      "domain": "measurement",
      "eventName": "Measurement_vIsbcMmc",
      "reportingEntityId": "cc305d54-75b4-431b-adb2-eb6b9e541234",
      "nfcNamingCode": "ssc",
      "nfNamingCode": "ibcx"
    },
    "measurementFields": {
      "measurementInterval": 5,
      "measurementFieldsVersion": "4.0",
      "additionalMeasurements": [
        {
          "name": "latency",
          "hashMap": {
            "value": "86"
          }
        },
        {
          "name": "throughput",
          "hashMap": {
            "value": "25"
          }
        },
        {
          "name": "identifier",
          "hashMap": {
            "value": "Cell1"
          }
        },
        {
          "name": "trafficModel",
          "hashMap": {
            "mobile_samsung_s10_01": "10"
          }
        }
      ]
    }
  }
}
```

Information about performance is situated in "additionalMeasurements" section, consisting of latency and throughput
parameters of performance, identifier of event producer and trafficModel with user equipment information. DataCollector
R-APP provides two endpoints to access data of stored VES Events:

1. `{datacollectorhost}/v1/pm/ues`

Returns list of user equipments from VES Events stored into database:

```json
{
  "ues": [
    "mobile_samsung_s10_01"
  ]
}
```

2. `{datacollectorhost}/v1/pm/events/aggregatedmetrics`

To call the endpoint you have to define 3 query parameters:

1.Slot - aggregation period (in seconds) for which an average performance metrics are calculated

2.Count - number of aggregated performance metrics that should be returned by the method, one aggergated performance
metric per each slot. The first performance metrics is average metrics for (startTime, startTime +slot)

3.startTime - ISO 8601 time format as string (e.g., 2020-10-26T06:52:54.01+00:00) for which aggregated performance
metrics are calculated with the pm ves data starting from startTime. "+" and "." signs must be properly encoded in url

Example
URL: `{{datacollectorhost}}/v1/pm/events/aggregatedmetrics?slot=10&count=12&startTime=2021-03-31T15%3A00%3A00.0Z`
This request will return aggregated metrics from two minutes starting from 2021.03.31-15:30:00.0

Aggregated Metrics response example:

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

# API

The API is documented by the Swagger tool.

## Swagger

JSON file that can be imported to Swagger GUI can be found in *doc/swagger*. Those files are regenerated in each maven
build, so to generate this file please see **Build DataCollector R-APP** chapter.

# Developer Guide

## Build DataCollector R-APP

Following mvn command (in the current directory) will build DataCollector R-APP:

```bash
mvn clean install
```

## Run DataCollector R-APP

Following command will run DataCollector R-APP:

```bash
java -jar datacollector-0.0.1-SNAPSHOT.jar org.onap.rapp.DataCollectorApplication
```

## Logging

The log file will be created in the /log path. Parameters of logging are in application.yml file.
After DataCollector R-APP starts successfully, log/rapp-datacollector.log should start to contain the logs:

```
.
└──log
    └── rapp-datacollector.log
```
