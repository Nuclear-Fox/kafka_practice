package com.prosoft;

import com.prosoft.config.KafkaConfig;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.errors.TopicExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class KafkaAdminApp {

    private static final Logger logger = LoggerFactory.getLogger(KafkaAdminApp.class);

    public static void main(String[] args) {
        createTopics(List.of("my-topic99", "my-topic999", "my-topic999"));
        getTopicsList();
        deleteTopic("my-topic3");
        getTopicsList();
    }

    /**
     * createTopics() - статический метод для создания топиков.
     *
     * @param topicNames Коллекция названий топиков для создания.
     */
    private static void createTopics(List<String> topicNames) {
        try (AdminClient adminClient = AdminClient.create(KafkaConfig.getAdminConfig())) {

            /** Получение списка существующих топиков */
            Set<String> existingTopics = adminClient.listTopics().names().get();

            /** Фильтрация названий топиков, которые уже существуют */
            List<String> newTopicNames = topicNames.stream()
                    .filter(topicName -> !existingTopics.contains(topicName))
                    .toList();

            if (newTopicNames.isEmpty()) {
                logger.info("Нет топиков для создания.");
                return;
            }

            /** Количество партиций для топика */
            int numPartitions = 3;

            /** Фактор репликации для топика (replication factor) определяет, сколько копий (реплик) каждого раздела (partition) топика хранится на различных брокерах Kafka в кластере
             * 1 - каждый раздел топика будет иметь одну реплику, которая является лидером. В этом случае отказоустойчивость не обеспечивается, так как нет дублирования данных на других брокерах;
             */
            short replicationFactor = 1;

            /** Создание топиков из коллекции названий */
            List<NewTopic> newTopics = topicNames.stream()
                    .map(topicName -> new NewTopic(topicName, numPartitions, replicationFactor))
                    .toList();

            createTopics(adminClient, newTopics, newTopicNames);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Ошибка при создании топика", e);
            Thread.currentThread().interrupt();
        }
    }

    private static void createTopics(AdminClient adminClient, List<NewTopic> newTopics, List<String> newTopicNames) throws InterruptedException, ExecutionException {
        try {
            adminClient.createTopics(newTopics).all().get();
            logger.info("Топики '{}' успешно созданы.", newTopicNames);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TopicExistsException) {
                logger.warn("Некоторые топики уже существуют: {}", e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * getTopicsList() - статический метод для получения списка топиков.
     */
    public static void getTopicsList() {
        try (AdminClient adminClient = AdminClient.create(KafkaConfig.getAdminConfig())) {

            /** Получение списка всех топиков */
            ListTopicsResult listTopicsResult = adminClient.listTopics();
            KafkaFuture<Set<String>> namesFuture = listTopicsResult.names();

            Set<String> topicNames = namesFuture.get();
            logger.info("Обнаружены топики: {}", topicNames);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Ошибка при получении топиков", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * deleteTopic() - статический метод для удаления топиков.
     */
    public static void deleteTopic(String topicName) {

        try (AdminClient adminClient = AdminClient.create(KafkaConfig.getAdminConfig())) {

            DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(List.of(topicName));

            KafkaFuture<Void> allDeletedFuture = deleteTopicsResult.all();

            /** Обработка результата удаления */
            allDeletedFuture.whenComplete((result, exception) -> {
                if (exception == null) {
                    logger.info("Топик {} успешно удален.", topicName);
                } else {
                    logger.error("Ошибка при удалении топика", exception);
                }
            });

                /** Ожидание завершения асинхронной операции (блокирующий вызов) */
                allDeletedFuture.get();

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Ошибка при удалении топика", e);
            Thread.currentThread().interrupt();
        }
    }

}
