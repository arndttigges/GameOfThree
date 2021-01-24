package com.takeaway.game.rule;

import com.takeaway.game.dto.GameMove;
import com.takeaway.game.repository.model.Action;
import com.takeaway.game.repository.model.Game;

public interface Rule {

    boolean isAllowedMove(Game game, GameMove move);
    int calcNewValue(int oldValue, Action action);

}
