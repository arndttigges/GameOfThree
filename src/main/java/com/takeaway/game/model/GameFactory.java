package com.takeaway.game.model;

import com.takeaway.game.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.LinkedList;
import java.util.UUID;

public class GameFactory {

    public static Game createNewGame(GameMode mode, Player opponent, Player firstMovePlayer, int startValue) {

        return Game.builder()
                .id(UUID.randomUUID())
                .mode(mode)
                .opponent(opponent)
                .movements(new LinkedList<>(List.of(firstMove(startValue, firstMovePlayer))))
                .status(mode == GameMode.REMOTE && opponent.equals(firstMovePlayer) ? GameStatus.READY : GameStatus.WAITING)
                .build();
    }

    private static Movement firstMove(int startValue, Player player){
        return Movement.builder()
                .player(player)
                .number(startValue)
                .movementSequenzNumber(1)
                .build();
    }


}
