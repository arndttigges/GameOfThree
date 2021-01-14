package com.takeaway.game.dto;

import com.takeaway.game.model.Action;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Move {

    private int sequenceNumber;
    private Action myAction;
    private int number;
    private Action opponentAction;

}
