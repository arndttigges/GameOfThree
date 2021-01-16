package com.takeaway.game.rule;

import com.takeaway.game.dto.GameMove;
import com.takeaway.game.model.Action;
import com.takeaway.game.model.Game;
import com.takeaway.game.model.Movement;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class DivideByThreeRule implements Rule {

    @Override
    public boolean isAllowedMove(Game game, GameMove move) {
        Movement lastMovement = game.getMovements().get(game.getMovements().size() - 1);

        boolean isDifferentUser = !lastMovement.getPlayer().getId().equals(move.getPlayerId());
        boolean valueIsHighEnough = lastMovement.getNumber() > 1;

        return isDifferentUser && valueIsHighEnough;
    }

    @Override
    public int calcNewValue(int oldValue, Action action) {
        return (oldValue + action.getValue()) / 3;
    }
}
