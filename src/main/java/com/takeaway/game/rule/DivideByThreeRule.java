package com.takeaway.game.rule;

import com.takeaway.game.dto.GameMove;
import com.takeaway.game.model.Action;
import com.takeaway.game.model.Game;
import com.takeaway.game.model.Movement;
import com.takeaway.game.model.Player;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class DivideByThreeRule implements Rule{


    public Movement apply(Game game, GameMove move) {
        Movement lastMovement = getLastMovement(game);
        Player player = game.getOpponent();

        int numberAfterAction = lastMovement.getNumber() + move.getMove().getValue();
        int newNumber = numberAfterAction / 3;

        return Movement.builder()
                .player(player)
                .number(newNumber)
                .action(move.getMove())
                .movementSequenzNumber(lastMovement.getMovementSequenzNumber() + 1)
                .build();
    }

    private Movement getLastMovement(Game game) {
        int lastElementIndex = game.getMovements().size() - 1;
        return game.getMovements().get(lastElementIndex);
    }

    @Override
    public Boolean isAllowedMove(Game game, GameMove move) {
        boolean isDifferentUser = game.getOpponent().toString().equals(move.getPlayerId());
        boolean valueIsHighEnough = getLastMovement(game).getNumber() > 1;

        return isDifferentUser && valueIsHighEnough;
    }

    @Override
    public int calcNewValue(int oldValue, Action action) {
        return (oldValue + action.getValue()) / 3;
    }
}
