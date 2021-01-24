# GameOfThree

This program is implementing the coding challenge from takeaway.com

# Pre Requirements

you need:

* java 15 (might also work with older versions)
* maven 3
* kafka broker
* docker (optional)

The programm can be build with following command:

```shell
mvn clean verify
```

## Running the Programm

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

# How To Play

## Create a Game

We have two options: remote and local

### Local Mode

Start a local game using the "Create Local Game against the Computer" section. Choose a starting number and click on "
Create Local Game" The game will then be listed under "Running Games"

### Remote Game

If you want to participate in a remote game, you can send a message that you are ready to play or you can reply to a
message from another player. Announcements from other players can be found under "Available Player" and remote games
under "Running Games".

Have Fun

# Misc

In the following I have several thoughts / notes on the implementation:

## Kafka

I have never set up Kafka before and used it very little. However, I found it interesting to solve the coding challenge
with it.

* I had problems with the conversion of the Kafka messages to my DTOs. After some time I had given up and hereby accept
  the minus point
* the Kafka settings are very simple and guaranteed not optimal
* Kafka messages do not have any authentication etc. The identification is done via the gameId
* I have a consistency problem getting and creating games from other players. I think there are good solutions for this,
  possibly with the callbacks. But I have little experience and have decided for the naive variant to solve the
  problem. (delete it again)
* i do not test kafka in a test directly. a ct test should be added.

## GUI

* To keep the handling as simple as possible I used the SessionId as user identification. This has the consequence that
  you have to use 2 browsers or a browser in incognito mode for 2 instances on one computer. Otherwise the game gets
  confused
* Maybe it would have been better to solve the identity via a login etc, but I didn't want to increase the complexity
  even more
* GUI tests are missing and the GameControllerTest needs more verifications of the responses.

