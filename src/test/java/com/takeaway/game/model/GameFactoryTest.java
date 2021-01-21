package com.takeaway.game.model;

import com.takeaway.game.service.GameFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameFactoryTest {

    private static final String PLAYER_A = "A";
    private static final String PLAYER_B = "B";

    private static final int START_VALUE = 42;
    private static final Mode GAME_MODE = Mode.LOCAL;

    @Test
    void createValidNewGame() {
        Game game = GameFactory.createNewGame(GAME_MODE, PLAYER_A, PLAYER_B, START_VALUE);
        assertAll(
                () -> assertFalse(game.getMovements().isEmpty()),
                () -> assertEquals(1, game.getMovements().get(0).getMovementSequenzNumber()),
                () -> assertEquals(PLAYER_B, game.getMovements().get(0).getPlayerId()),
                () -> assertEquals(START_VALUE, game.getMovements().get(0).getNumber()),
                () -> assertEquals(GAME_MODE, game.getMode()),
                () -> assertEquals(PLAYER_A, game.getOpponentId()),
                () -> assertEquals(Status.WAITING, game.status)
        );
    }

    @Test
    void movementListCanBeModified() {
        Game game = GameFactory.createNewGame(GAME_MODE, PLAYER_A, PLAYER_B, START_VALUE);
        game.getMovements().add(Movement.builder().build());
        assertEquals(2, game.getMovements().size());
    }

    @Test
    void incomingRemoteGameIsInReadyStatus() {
        Game game = GameFactory.createNewGame(Mode.REMOTE, PLAYER_A, PLAYER_A, START_VALUE);
        assertEquals(Status.READY, game.getStatus());
    }

    @Test
    void outgoingRemoteGameIsInWaitingStatus() {
        Game game = GameFactory.createNewGame(Mode.REMOTE, PLAYER_A, PLAYER_B, START_VALUE);
        assertEquals(Status.WAITING, game.getStatus());
    }
}