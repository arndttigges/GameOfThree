package com.takeaway.game.dto;

import com.takeaway.game.model.Action;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Move {

    private final int sequenceNumber;
    private final Action myAction;
    private final int number;
    private final Action opponentAction;

}
