# Kafka streams reproducer (Legacy handler 2.8+ doesn't stop Kafka streams instance)

## How to start ? 

``` bash
./start.sh
```

- This script will deploy one broker and one zookeper, wait 10 seconds and create topics need for the Kafka streams application.
- At the end of the script, you have to publish one record into the 'input' topic
- Run kafka streams application. You can see app crash but the state still RUNNING
- Uncomment line L47 in Main.java
- Re-run kafka streams application. Topology crash and application will stop.