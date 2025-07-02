package com.prosoft;

import com.prosoft.config.KafkaConfig;
import com.prosoft.domain.Symbol;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;

/**
 * Webinar-03: Kafka consumer-service (прием экземпляров класса Person из topic2)
 * Использования метода consumer.poll().
 */
public class KafkaConsumerApp {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerApp.class);
    private static final Duration TEN_MILLISECONDS_INTERVAL = Duration.ofMillis(10);

    public static void main(String[] args) {

        /***
         * Создание и запуск консюмеров в трех потоках
         */
        for (int i = 0; i < KafkaConfig.NUM_CONSUMERS; i++) {

            new Thread(() -> {
                KafkaConsumer<Long, Symbol> consumerVowels = new KafkaConsumer<>(KafkaConfig.getConsumerConfig());

                consumerVowels.subscribe(Collections.singletonList(KafkaConfig.TOPIC_VOWELS));


                /** Возможно использовать try-with-resources */
                try {
                    while (true) {
                        ConsumerRecords<Long, Symbol> records = consumerVowels.poll(TEN_MILLISECONDS_INTERVAL);
                        for (ConsumerRecord<Long, Symbol> cr : records) {
                            logger.info("Consumer {}. Received message: key={}, value={}, partition={}, offset={}",
                                    Thread.currentThread().getName(), cr.key(), cr.value(), cr.partition(), cr.offset());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred in consumer thread", e);
                } finally {
                    consumerVowels.close();
                }
            }).start();

//            new Thread(() -> {
//                KafkaConsumer<Long, Symbol> consumerConsonants = new KafkaConsumer<>(KafkaConfig.getConsumerConfig());
//
//                consumerConsonants.subscribe(Collections.singletonList(KafkaConfig.TOPIC_CONSONANTS));
//
//
//                /** Возможно использовать try-with-resources */
//                try {
//                    while (true) {
//                        ConsumerRecords<Long, Symbol> records = consumerConsonants.poll(TEN_MILLISECONDS_INTERVAL);
//                        for (ConsumerRecord<Long, Symbol> cr : records) {
//                            logger.info("Consumer {}. Received message: key={}, value={}, partition={}, offset={}",
//                                    Thread.currentThread().getName(), cr.key(), cr.value(), cr.partition(), cr.offset());
//                        }
//                    }
//                } catch (Exception e) {
//                    logger.error("Exception occurred in consumer thread", e);
//                } finally {
//                    consumerConsonants.close();
//                }
//            }).start();

        }
    }

}
