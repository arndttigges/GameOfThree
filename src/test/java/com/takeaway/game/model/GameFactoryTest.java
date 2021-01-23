package com.takeaway.game.model;

import com.takeaway.game.dto.GameOverviewElement;
import com.takeaway.game.dto.Move;
import com.takeaway.game.service.GameFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

class GameFactoryTest {

    private static final String PLAYER_A = "A";
    private static final String PLAYER_B = "B";

    private static final int START_VALUE = 42;
    private static final Mode GAME_MODE = Mode.LOCAL;

    @Test
    void createValidNewGame() {
        Game game = GameFactory.createNewGame(GAME_MODE,PLAYER_B, PLAYER_A, PLAYER_B, START_VALUE);
        assertAll(
                () -> assertFalse(game.getMovements().isEmpty()),
                () -> assertEquals(1, game.getMovements().get(0).getMovementSequenzNumber()),
                () -> assertEquals(PLAYER_B, game.getMovements().get(0).getPlayerId()),
                () -> assertEquals(START_VALUE, game.getMovements().get(0).getNumber()),
                () -> assertEquals(GAME_MODE, game.getMode()),
                () -> assertEquals(PLAYER_A, game.getOpponentId())
        );
    }

    @Test
    void movementListCanBeModified() {
        Game game = GameFactory.createNewGame(GAME_MODE,PLAYER_B, PLAYER_A, PLAYER_B, START_VALUE);
        game.getMovements().add(Movement.builder().build());
        assertEquals(2, game.getMovements().size());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "A, B, ZERO, 2, 1, FINISHED",
            "B, A, PLUS, 3, 32, READY"
    })
    void determineGameStatusTest(String player, String opponent, Action lastAction, int sequenceNumber, int value, Status status) {
        List<Game> gameList = List.of(
                GameFactory.createNewGame(Mode.LOCAL, player, opponent, PLAYER_A, START_VALUE)
        );
        Movement movement = Movement.builder().playerId(PLAYER_B).movementSequenzNumber(sequenceNumber).action(lastAction).number(value).build();
        Game game = gameList.get(0);
        game.getMovements().add(movement);
        List<GameOverviewElement> overviewElements = GameFactory.convertToGameView(gameList, PLAYER_A);


        GameOverviewElement element = overviewElements.get(0);
        assertAll(
                () -> assertEquals(game.getId(), element.getUuid()),
                () -> assertEquals(opponent, element.getOpponent()),
                () -> assertEquals(lastAction, element.getLastStep()),
                () -> assertEquals(value, element.getNumber()),
                () -> assertEquals(sequenceNumber, element.getSequenceNumber()),
                () -> assertEquals(status, element.getStatus())
        );

    }

    @ParameterizedTest
    @CsvSource(value = {
            "A,1, FINISHED",
            "B, 5, WAITING",
            "A, 5, READY"
    })
    void determineGameStatusTest(String playerId, int number, Status result) {
        Game game = GameFactory.createNewGame(null, playerId, PLAYER_B, PLAYER_A, START_VALUE);
        game.getMovements().add(Movement.builder().playerId(PLAYER_B).number(number).build());

        Status status = GameFactory.determineGameStatus(game, playerId);
        assertEquals(result, status);
    }
}