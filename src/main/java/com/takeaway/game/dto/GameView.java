package com.takeaway.game.dto;

import com.takeaway.game.model.Action;
import com.takeaway.game.model.GameStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class GameView {

    private final UUID uuid;
    private final String opponent;
    private final int sequenceNumber;
    private final int number;
    private final GameStatus status;
    private final Action lastStep;

}
