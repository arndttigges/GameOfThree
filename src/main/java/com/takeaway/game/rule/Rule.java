package com.takeaway.game.rule;

import com.takeaway.game.dto.GameMove;
import com.takeaway.game.model.Action;
import com.takeaway.game.model.Game;
import com.takeaway.game.model.Movement;

public interface Rule {

    Boolean isAllowedMove(Game game, GameMove move);
    int calcNewValue(int oldValue, Action action);

}
