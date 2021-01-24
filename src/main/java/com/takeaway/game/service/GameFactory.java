package com.takeaway.game.service;

import com.takeaway.game.dto.DetailedGameView;
import com.takeaway.game.dto.DetailedGameViewTableElement;
import com.takeaway.game.dto.GameOverviewElement;
import com.takeaway.game.repository.model.Game;
import com.takeaway.game.repository.model.Mode;
import com.takeaway.game.repository.model.Movement;
import com.takeaway.game.repository.model.Status;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameFactory {

    public static Game createNewGame(Mode mode, String player, String opponent, String firstMovePlayer, int startValue) {
        return Game.builder()
                .id(UUID.randomUUID())
                .mode(mode)
                .opponentId(opponent)
                .playerId(player)
                .movements(new LinkedList<>(List.of(createFirstMove(startValue, firstMovePlayer))))
                .build();
    }

    public static List<GameOverviewElement> convertToGameView(List<Game> games, String userId) {
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
                            .status(determineGameStatus(game, userId))
                            .build();
                })
               .collect(Collectors.toList());
    }

    public static DetailedGameView createDetailedGameFromGame(Game game, String userId) {
        List<DetailedGameViewTableElement> detailedGameViewTableElements = game.getMovements()
                .stream()
                .map(movement -> convertMovementToMove(movement, userId))
                .collect(Collectors.toList());

        return DetailedGameView.builder()
                .uuid(game.getId())
                .status(determineGameStatus(game, userId))
                .movements(detailedGameViewTableElements).build();
    }

    private static Movement createFirstMove(int startValue, String player) {
        return Movement.builder()
                .playerId(player)
                .number(startValue)
                .movementSequenzNumber(1)
                .build();
    }

    private static DetailedGameViewTableElement convertMovementToMove(Movement movement, String userId) {
        DetailedGameViewTableElement.DetailedGameViewTableElementBuilder builder = DetailedGameViewTableElement.builder()
                .sequenceNumber(movement.getMovementSequenzNumber())
                .number(movement.getNumber());
        if (movement.getPlayerId() != null) {
            if (movement.getPlayerId().equals(userId)) {
                builder.myAction(movement.getAction());
            } else {
                builder.opponentAction(movement.getAction());
            }
        }

        return builder.build();
    }

    public static Status determineGameStatus(Game game, String userId) {
        Movement lastMovement = game.getMovements().get(game.getMovements().size() - 1);

        if (lastMovement.getNumber() <= 1) return Status.FINISHED;
        if (lastMovement.getPlayerId().equals(userId)) return Status.WAITING;

        return Status.READY;
    }

}
