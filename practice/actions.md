`1.` Запуск контейнеров Docker:
```shell
docker compose up -d
```

`2.` Получаем список Топиков которые есть в Kafka брокере, доступном по адресу kafka:9092 и находящемся внутри контейнера Docker с именем kafka:
```shell
docker exec -ti kafka1 /usr/bin/kafka-topics --list --bootstrap-server localhost:9091
```

`3.` Создаем новый топик "vowels"
```shell
docker exec -ti kafka1 /usr/bin/kafka-topics --create --topic vowels --replication-factor 3 --bootstrap-server localhost:9091
```

`4.` Создаем новый топик "consonants"
```shell
docker exec -ti kafka1 /usr/bin/kafka-topics --create --topic consonants --replication-factor 3 --bootstrap-server localhost:9091
```

`5.` Отправляем сообщение в "vowels"
```shell
docker exec -ti kafka /usr/bin/kafka-console-producer --topic vowels --bootstrap-server kafka:9093
```

`6.` Отправляем сообщение в "consonants"
```shell
docker exec -ti kafka /usr/bin/kafka-console-producer --topic consonants --bootstrap-server kafka:9093
```

`7.` Получить сообщения из "vowels"
```shell
docker exec -ti kafka /usr/bin/kafka-console-consumer --from-beginning --topic vowels --bootstrap-server localhost:9093
```

`8.` Получить сообщения из "consonants"
```shell
docker exec -ti kafka /usr/bin/kafka-console-consumer --from-beginning --topic consonants --bootstrap-server localhost:9093
```

`9.` Останавливаем контейнеры, удаляем контейнеры, удаляем неиспользуемые тома:
```shell
docker compose stop
docker container prune -f
docker volume prune -f
```
