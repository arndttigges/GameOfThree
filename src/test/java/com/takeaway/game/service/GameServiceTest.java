package com.takeaway.game.service;

import com.takeaway.game.dto.DetailedGameView;
import com.takeaway.game.dto.GameMove;
import com.takeaway.game.dto.GameOverviewElement;
import com.takeaway.game.dto.GameTemplate;
import com.takeaway.game.repository.GameRepository;
import com.takeaway.game.repository.model.*;
import com.takeaway.game.rule.RuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    RuleEngine ruleEngine;
    GameService gameService;
    @Mock
    private RequestAttributes requestAttributes;

    @BeforeEach
    void init() {
        gameService = new GameService(gameRepository, ruleEngine);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    @Test
    void fetchGame() {
        Game game = GameFactory.createNewGame(Mode.LOCAL, PLAYER_A, PLAYER_B, PLAYER_A, START_VALUE);
        when(gameRepository.findById(any(UUID.class))).thenReturn(
                Optional.of(game));
        DetailedGameView detailedGameView = gameService.fetchGame(game.getId());

        assertAll(
                () -> assertEquals(game.getId(), detailedGameView.getUuid()),
                () -> assertEquals(Status.READY, detailedGameView.getStatus()),
                () -> assertEquals(1, detailedGameView.getMovements().size())
        );
    }

    @Test
    void doingLocalMove() {
        Game testGame = GameFactory.createNewGame(Mode.LOCAL, PLAYER_B, PLAYER_A, PLAYER_A, START_VALUE);
        GameMove testMove = new GameMove();
        testMove.setPlayerId(PLAYER_B);
        testMove.setAction(Action.ZERO);

        when(ruleEngine.executeMove(any(Game.class), any(GameMove.class)))
                .thenReturn(Optional.of(createTestMovement()));
        when(gameRepository.save(testGame)).thenReturn(testGame);
        when(requestAttributes.getSessionId()).thenReturn(PLAYER_A);

        DetailedGameView movements = gameService.performMove(testGame, testMove);

        assertAll(
                () -> assertEquals(3, movements.getMovements().size()),
                () -> assertEquals(Status.READY, movements.getStatus())
        );

        verify(gameRepository).save(testGame);
        verify(ruleEngine, times(2)).executeMove(any(Game.class), any(GameMove.class));
    }

    @Test
    void doingRemoteMove() {
        Game testGame = GameFactory.createNewGame(Mode.REMOTE, PLAYER_B, PLAYER_A, PLAYER_A, START_VALUE);
        GameMove testMove = new GameMove();
        testMove.setPlayerId(PLAYER_B);
        testMove.setAction(Action.ZERO);

        when(ruleEngine.executeMove(testGame, testMove))
                .thenReturn(Optional.of(createTestMovement()));
        when(gameRepository.save(testGame)).thenReturn(testGame);
        when(requestAttributes.getSessionId()).thenReturn(PLAYER_B);

        DetailedGameView movements = gameService.performMove(testGame, testMove);

        assertAll(
                () -> assertEquals(2, movements.getMovements().size()),
                () -> assertEquals(Status.WAITING, movements.getStatus())
        );

        verify(gameRepository).save(testGame);
        verify(ruleEngine).executeMove(testGame, testMove);
    }

    @Test
    void createAValidNewLocalGame() {
        when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArguments()[0]);

        GameTemplate game = new GameTemplate(START_VALUE);
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

        Game result = gameService.createNewRemoteGame(UUID.randomUUID(), PLAYER_B, PLAYER_A, PLAYER_A, START_VALUE);

        assertAll(
                () -> assertEquals(Mode.REMOTE, result.getMode()),
                () -> assertEquals(START_VALUE, result.getMovements().get(0).getNumber()),
                () -> assertEquals(PLAYER_A, result.getOpponentId())
        );

        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void performRemoteMove() {
        Game testGame = GameFactory.createNewGame(Mode.REMOTE, PLAYER_B, PLAYER_A, PLAYER_A, START_VALUE);
        GameMove testMove = new GameMove();
        testMove.setPlayerId(PLAYER_B);
        testMove.setAction(Action.ZERO);

        when(gameRepository.findById(testGame.getId())).thenReturn(Optional.of(testGame));
        gameService.performRemoteMove(testGame.getId(), 2, testMove);

        verify(gameRepository).findById(testGame.getId());
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void doNotPerformMoveWhenSequenzNumberIsAlreadyThere() {
        Game testGame = GameFactory.createNewGame(Mode.REMOTE, PLAYER_B, PLAYER_A, PLAYER_A, START_VALUE);
        GameMove testMove = new GameMove();
        testMove.setPlayerId(PLAYER_B);
        testMove.setAction(Action.ZERO);

        when(gameRepository.findById(testGame.getId())).thenReturn(Optional.of(testGame));
        gameService.performRemoteMove(testGame.getId(), 1, testMove);

        verify(gameRepository).findById(testGame.getId());
        verify(gameRepository, times(0)).save(any(Game.class));
        verify(requestAttributes, times(0)).getSessionId();
    }

    @Test
    void showAllValidGames() {
        List<Game> games = List.of(
                GameFactory.createNewGame(Mode.REMOTE, PLAYER_B, PLAYER_A, PLAYER_A, START_VALUE),
                GameFactory.createNewGame(Mode.REMOTE, PLAYER_B, PLAYER_A, PLAYER_B, START_VALUE),
                GameFactory.createNewGame(Mode.REMOTE, PLAYER_A, PLAYER_B, PLAYER_B, START_VALUE),
                GameFactory.createNewGame(Mode.REMOTE, PLAYER_A, PLAYER_B, PLAYER_A, START_VALUE)
        );

        when(requestAttributes.getSessionId()).thenReturn(PLAYER_A);
        when(gameRepository.findAll()).thenReturn(games);

        List<GameOverviewElement> elements = gameService.getAllRunningGames();
        assertEquals(4, elements.size());

        verify(requestAttributes, atLeastOnce()).getSessionId();
        verify(gameRepository).findAll();
    }

    @Test
    void filterInvalidGames() {
        String unknownPlayer = "C";
        List<Game> games = List.of(
                GameFactory.createNewGame(Mode.REMOTE, unknownPlayer, PLAYER_A, PLAYER_A, START_VALUE),
                GameFactory.createNewGame(Mode.REMOTE, PLAYER_B, unknownPlayer, PLAYER_B, START_VALUE),
                GameFactory.createNewGame(Mode.REMOTE, PLAYER_A, unknownPlayer, PLAYER_B, START_VALUE),
                GameFactory.createNewGame(Mode.REMOTE, unknownPlayer, unknownPlayer, unknownPlayer, START_VALUE)
        );

        when(requestAttributes.getSessionId()).thenReturn(PLAYER_A);
        when(gameRepository.findAll()).thenReturn(games);

        List<GameOverviewElement> elements = gameService.getAllRunningGames();
        assertEquals(2, elements.size());

        verify(requestAttributes, atLeastOnce()).getSessionId();
        verify(gameRepository).findAll();
        verify(gameRepository).deleteAll(anyIterable());
    }

    private Movement createTestMovement() {
        return Movement.builder().playerId(PLAYER_B).number(7).build();
    }

}