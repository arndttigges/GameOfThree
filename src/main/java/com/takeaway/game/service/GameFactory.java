package com.takeaway.game.service;

import com.takeaway.game.dto.GameMovements;
import com.takeaway.game.dto.GameOverviewElement;
import com.takeaway.game.dto.Move;
import com.takeaway.game.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameFactory {

    public static Game createNewGame(Mode mode, String opponent, String firstMovePlayer, int startValue) {
        return Game.builder()
                .id(UUID.randomUUID())
                .mode(mode)
                .opponentId(opponent)
                .movements(new LinkedList<>(List.of(createFirstMove(startValue, firstMovePlayer))))
                .status(mode == Mode.REMOTE && opponent.equals(firstMovePlayer) ? Status.READY : Status.WAITING)
                .build();
    }

    public static List<GameOverviewElement> convertToGameView(List<Game> games) {
       return games
                .stream()
                .map(game -> {
                    Movement lastMove = game.getMovements().get(game.getMovements().size() - 1);
                    return GameOverviewElement.builder()
                            .uuid(game.getId())
                            .opponent(game.getOpponentId())
                            .lastStep(lastMove.getAction())
                            .sequenceNumber(lastMove.getMovementSequenzNumber())
                            .number(lastMove.getNumber())
                            .status(game.getStatus())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public static GameMovements createDetailedGameFromGame(Game game, String userId) {
        List<Move> moves = game.getMovements()
                .stream()
                .map(movement -> convertMovementToMove(movement, userId))
                .collect(Collectors.toList());

        return GameMovements.builder().uuid(game.getId()).status(game.getStatus()).movements(moves).build();
    }

    private static Movement createFirstMove(int startValue, String player) {
        return Movement.builder()
                .playerId(player)
                .number(startValue)
                .movementSequenzNumber(1)
                .build();
    }

    private static Move convertMovementToMove(Movement movement, String userId) {
        Move.MoveBuilder builder = Move.builder()
                .sequenceNumber(movement.getMovementSequenzNumber())
                .number(movement.getNumber());
        if(movement.getPlayerId() != null) {
            if(movement.getPlayerId().equals(userId)) {
                builder.myAction(movement.getAction());
            } else {
                builder.opponentAction(movement.getAction());
            }
        }

        return builder.build();
    }


}
