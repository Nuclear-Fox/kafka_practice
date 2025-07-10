package com.prosoft;

import com.prosoft.config.KafkaConfig;
import com.prosoft.domain.Symbol;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class KafkaProducerApp {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerApp.class);
    private static final int MAX_MESSAGE = 10;
    private static final char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final String COLOR_DEFAULT = "#000000";

    public static void main(String[] args) {
        KafkaProducer<Long, Symbol> producer = new KafkaProducer<>(KafkaConfig.getProducerConfig());
        try {
            producer.initTransactions();

            producer.beginTransaction();
            for (int i = 0; i < alphabet.length; i++) {
                Symbol symbol = new Symbol(i, String.valueOf(alphabet[i]), COLOR_DEFAULT, "Char");

                long timestamp = System.currentTimeMillis();
                Matcher match = Pattern.compile("[aioeuy]").matcher(symbol.getValue());

                ProducerRecord<Long, Symbol> producerRecord;
                if (match.find()) {
                    producerRecord = new ProducerRecord<>(KafkaConfig.TOPIC_VOWELS, symbol);
                } else {
                    producerRecord = new ProducerRecord<>(KafkaConfig.TOPIC_CONSONANTS, symbol);
                }

                producer.send(producerRecord, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if (e != null) {
                            logger.error("Error sending message: {}", e.getMessage(), e);
                        } else {
                            logger.info("Sent record: key={}, value={}, partition={}, offset={}",
                                    symbol.getId(), symbol, recordMetadata.partition(), recordMetadata.offset());                        }
                    }
                });
                logger.info("Отправлено сообщение: key-{}, value-{}", i, symbol);
            }
            logger.info("Отправка завершена.");

            producer.commitTransaction();
        } catch (Exception e) {
            producer.abortTransaction();
            logger.error("Ошибка при отправке сообщений в Kafka", e);
        }
    }
}
