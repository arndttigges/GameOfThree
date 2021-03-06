package com.takeaway.game.dto;

import com.takeaway.game.repository.model.Action;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DetailedGameViewTableElement {

    private final int sequenceNumber;
    private final Action myAction;
    private final int number;
    private final Action opponentAction;

}
