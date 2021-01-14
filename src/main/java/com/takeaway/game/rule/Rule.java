package com.takeaway.game.rule;

import com.takeaway.game.dto.GameMove;
import com.takeaway.game.model.Game;
import com.takeaway.game.model.Movement;

public interface Rule {

    Movement apply(Game game, GameMove move);

}
