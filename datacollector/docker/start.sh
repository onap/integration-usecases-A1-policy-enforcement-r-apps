#!/usr/bin/env bash

# Initialize needed databases and tables if not already exist
while ! mysqladmin ping -u$DATABASE_USERNAME -p$DATABASE_PASSWORD -h$DATABASE_HOST --silent; do
    echo "$DATABASE_HOST not up, wait"
    sleep 1
done
mysql -u$DATABASE_USERNAME -p$DATABASE_PASSWORD -h$DATABASE_HOST < init.sql

exec java $JAVA_SEC_OPTS $JAVA_OPTS -jar /app/service.jar
