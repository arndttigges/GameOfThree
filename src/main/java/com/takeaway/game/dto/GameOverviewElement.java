package com.takeaway.game.dto;

import com.takeaway.game.repository.model.Action;
import com.takeaway.game.repository.model.Status;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class GameOverviewElement {

    private final UUID uuid;
    private final String opponent;
    private final int sequenceNumber;
    private final int number;
    private final Status status;
    private final Action lastStep;

}
