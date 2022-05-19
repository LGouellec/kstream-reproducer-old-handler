package org.example.streams.reproducer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Main {
    private static KafkaStreams streams;
    private static boolean stop = false;

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Properties prop = new Properties();
        prop.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        prop.put(StreamsConfig.APPLICATION_ID_CONFIG, "app-id");
        prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        prop.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        prop.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        prop.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);
        Object n = null;

        StreamsBuilder sb = new StreamsBuilder();
        sb.stream("input").mapValues((k,v) -> {
            n.toString(); // throw NPE
            return v;
        }
        ).to("output");

        streams = new KafkaStreams(sb.build(), prop);
        streams.setUncaughtExceptionHandler((t,e) -> {
            System.out.println("Old handler ..." + e.getMessage());
            // IF YOU UNCOMMENT, the application stop correctly
            // IF YOU KEEP COMMENTED, the application doesn't stop, state still RUNNING with none Stream thread alive
            // streams.close();
        });

        Main m = new Main();
        Thread thread = new Thread(m::runLoopMetrics);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop = true;
            streams.close();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        streams.start();
        thread.start();
    }

    private void runLoopMetrics() {
        while(!stop){

            if(streams.state() == KafkaStreams.State.ERROR || streams.state() == KafkaStreams.State.NOT_RUNNING)
                stop = true;

            Map<MetricName, ? extends Metric> metrics =  streams.metrics();

            Optional<? extends Metric> metricAliveStreamThread = streams.metrics().values().stream().filter((v) -> v.metricName().group().equals("stream-metrics") && v.metricName().name().equals("alive-stream-threads")).findFirst();
            Optional<? extends Metric> metricFailedStreamThread = streams.metrics().values().stream().filter((v) -> v.metricName().group().equals("stream-metrics") && v.metricName().name().equals("failed-stream-threads")).findFirst();

            if(metricAliveStreamThread.isPresent()) {
                System.out.println("Kafka streams status : " + streams.state().toString());
                System.out.println("Alive stream thread : " + metricAliveStreamThread.get().metricValue());
                System.out.println("Failed stream thread : " + metricFailedStreamThread.get().metricValue());
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
