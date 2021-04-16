FROM nexus3.onap.org:10001/onap/integration-java11:8.0.0

COPY sleepingcelldetector*.jar /app/service.jar
ENTRYPOINT exec java $JAVA_SEC_OPTS $JAVA_OPTS -jar /app/service.jar
