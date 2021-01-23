# GameOfThree

This program is implementing the coding challenge from takeaway.com

# Prerequirements

you need:

* java 15 (might also work with older versions)
* maven 3
* kafka broker
* docker (optional)

The programm can be build with following command:

```shell
mvn clean verify
```

## running the programm

### Kafka

you need a kafka broker. Change the settings in the `application.yml` to connect the program with your kafka. Or use
docker:

```shell
docker pull johnnypark/kafka-zookeeper
docker run -p 2181:2181 -p 9092:9092 -e ADVERTISED_HOST=127.0.0.1  -e NUM_PARTITIONS=10 johnnypark/kafka-zookeeper
```

### Spring app

There are several ways to run a Spring Boot application. One way is to execute the `main` method in
the `com.takeaway.game.GameApplication` class of the IDE. you can also use
the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html)
and start it via console:

```shell
mvn spring-boot:run
```
