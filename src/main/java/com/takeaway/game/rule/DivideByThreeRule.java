package com.takeaway.game.rule;

import com.takeaway.game.dto.GameMove;
import com.takeaway.game.repository.model.Action;
import com.takeaway.game.repository.model.Game;
import com.takeaway.game.repository.model.Movement;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class DivideByThreeRule implements Rule {

    @Override
    public boolean isAllowedMove(Game game, GameMove move) {
        Movement lastMovement = game.getMovements().get(game.getMovements().size() - 1);

        boolean isParticipant = move.getPlayerId().equals(game.getPlayerId()) || move.getPlayerId().equals(game.getOpponentId());
        boolean isDifferentUser = !lastMovement.getPlayerId().equals(move.getPlayerId());
        boolean valueIsHighEnough = lastMovement.getNumber() > 1;

        return isParticipant && isDifferentUser && valueIsHighEnough;
    }

    @Override
    public int calcNewValue(int oldValue, Action action) {
        return (oldValue + action.getValue()) / 3;
    }
}
