package com.example.NexusMart.utils;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Component
public class KafkaHealthCheck {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public boolean areMultipleBrokersAvailable() {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(config)) {
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            int brokerCount = clusterResult.nodes().get().size();
            return brokerCount > 1;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }
}
