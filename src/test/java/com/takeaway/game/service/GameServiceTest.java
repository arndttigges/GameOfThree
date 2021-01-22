package com.takeaway.game.service;

import com.takeaway.game.dto.GameMove;
import com.takeaway.game.dto.GameMovements;
import com.takeaway.game.dto.GameTemplate;
import com.takeaway.game.dto.NewRemoteGame;
import com.takeaway.game.kafka.KafkaService;
import com.takeaway.game.model.*;
import com.takeaway.game.repository.GameRepository;
import com.takeaway.game.rule.RuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    private static final int START_VALUE = 42;
    private static final String PLAYER_A = "PLAYER_A";
    private static final String PLAYER_B = "PLAYER_B";
    @Mock
    GameRepository gameRepository;
    @Mock
    KafkaService kafkaService;
    @Mock
    RuleEngine ruleEngine;
    GameService gameService;
    @Mock
    private RequestAttributes requestAttributes;

    @BeforeEach
    void init() {
        gameService = new GameService(gameRepository, kafkaService, ruleEngine);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    @Test
    void doingLocalMove() {
        Game testGame = GameFactory.createNewGame(Mode.LOCAL, PLAYER_B, PLAYER_A, PLAYER_A, START_VALUE);
        GameMove testMove = new GameMove();
        testMove.setPlayerId(PLAYER_B);
        testMove.setAction(Action.ZERO);

        when(ruleEngine.executeMove(any(Game.class), any(GameMove.class)))
                .thenReturn(Optional.of(createTestMovement(PLAYER_B, 7)));
        when(gameRepository.save(testGame)).thenReturn(testGame);
        when(requestAttributes.getSessionId()).thenReturn(PLAYER_A);

        GameMovements movements = gameService.performMove(testGame, testMove);

        assertAll(
                () -> assertEquals(3, movements.getMovements().size()),
                () -> assertEquals(Status.READY, movements.getStatus())
        );

        verify(gameRepository).save(testGame);
        verify(ruleEngine, times(2)).executeMove(any(Game.class), any(GameMove.class));
        verify(kafkaService, times(0)).sendMove(testGame.getId(), PLAYER_B, testMove.getAction(),2);
    }

    @Test
    void doingRemoteMove() {
        Game testGame = GameFactory.createNewGame(Mode.REMOTE, PLAYER_B, PLAYER_A, PLAYER_A, START_VALUE);
        GameMove testMove = new GameMove();
        testMove.setPlayerId(PLAYER_B);
        testMove.setAction(Action.ZERO);

        when(ruleEngine.executeMove(testGame, testMove))
                .thenReturn(Optional.of(createTestMovement(PLAYER_B, 7)));
        when(gameRepository.save(testGame)).thenReturn(testGame);
        when(requestAttributes.getSessionId()).thenReturn(PLAYER_B);

        GameMovements movements = gameService.performMove(testGame, testMove);

        assertAll(
                () -> assertEquals(2, movements.getMovements().size()),
                () -> assertEquals(Status.WAITING, movements.getStatus())
        );

        verify(gameRepository).save(testGame);
        verify(ruleEngine).executeMove(testGame, testMove);
        verify(kafkaService).sendMove(testGame.getId(), PLAYER_B, testMove.getAction(), 2);
    }

    @Test
    void createAValidNewLocalGame() {
        when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArguments()[0]);

        GameTemplate game = new GameTemplate(PLAYER_A, START_VALUE);
        Game result = gameService.createNewLocalGame(game);

        assertAll(
                () -> assertEquals(Mode.LOCAL, result.getMode()),
                () -> assertEquals(START_VALUE, result.getMovements().get(0).getNumber()),
                () -> assertNotEquals(PLAYER_A, result.getOpponentId())
        );

        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void createAValidNewRemoteGame() {
        when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArguments()[0]);

        NewRemoteGame game = new NewRemoteGame(START_VALUE, PLAYER_A);
        Game result = gameService.createNewRemoteGame(game);

        assertAll(
                () -> assertEquals(Mode.REMOTE, result.getMode()),
                () -> assertEquals(START_VALUE, result.getMovements().get(0).getNumber()),
                () -> assertEquals(PLAYER_A, result.getOpponentId())
        );

        verify(gameRepository).save(any(Game.class));
    }

    private Movement createTestMovement(String playerId, int number) {
        return Movement.builder().playerId(playerId).number(number).build();
    }

}