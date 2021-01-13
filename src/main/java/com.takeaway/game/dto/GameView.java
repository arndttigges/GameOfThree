package com.takeaway.game.dto;

import com.takeaway.game.model.Action;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GameView {

    private final String opponentAddress;
    private final int sequenceNumber;
    private final int number;
    private final Action lastStep;

}
