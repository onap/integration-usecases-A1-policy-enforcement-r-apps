FROM nexus3.onap.org:10001/onap/integration-java11:8.0.0

USER root

RUN apk add --no-cache mysql-client

USER onap

EXPOSE 8087

COPY datacollector*.jar /app/service.jar
COPY init.sql start.sh /app/
ENTRYPOINT ["/bin/sh", "/app/start.sh"]
