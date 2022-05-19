#!/bin/bash

docker-compose down -v
docker-compose up -d
sleep 10

brokerContainerId=`docker ps -q -f name=broker`

docker exec ${brokerContainerId} kafka-topics --bootstrap-server broker:29092 --topic input --create --partitions 3 1 > /dev/null 2>&1
docker exec ${brokerContainerId} kafka-topics --bootstrap-server broker:29092 --topic output --create --partitions 3 1 > /dev/null 2>&1
echo "Enter one message into the topic 'input'"
docker exec -it ${brokerContainerId} kafka-console-producer --bootstrap-server broker:29092 --topic input