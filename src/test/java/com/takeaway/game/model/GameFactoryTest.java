package com.takeaway.game.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameFactoryTest {

    private static final Player PLAYER_A = Player.builder().id("a").name("a").build();
    private static final Player PLAYER_B = Player.builder().id("b").name("b").build();
    private static final int START_VALUE = 42;
    private static final GameMode GAME_MODE = GameMode.LOCAL;

    @Test
    void createValidNewGame() {
        Game game = GameFactory.createNewGame(GAME_MODE, PLAYER_A, PLAYER_B, START_VALUE);
        assertAll(
                () -> assertFalse(game.getMovements().isEmpty()),
                () -> assertEquals(1, game.getMovements().get(0).getMovementSequenzNumber()),
                () -> assertEquals(PLAYER_B, game.getMovements().get(0).getPlayer()),
                () -> assertEquals(START_VALUE, game.getMovements().get(0).getNumber()),
                () -> assertEquals(GAME_MODE, game.getMode()),
                () -> assertEquals(PLAYER_A, game.getOpponent()),
                () -> assertEquals(GameStatus.WAITING, game.status)
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
        Game game = GameFactory.createNewGame(GameMode.REMOTE, PLAYER_A, PLAYER_A, START_VALUE);
        assertEquals(GameStatus.READY, game.getStatus());
    }

    @Test
    void outgoingRemoteGameIsInWaitingStatus() {
        Game game = GameFactory.createNewGame(GameMode.REMOTE, PLAYER_A, PLAYER_B, START_VALUE);
        assertEquals(GameStatus.WAITING, game.getStatus());
    }
}