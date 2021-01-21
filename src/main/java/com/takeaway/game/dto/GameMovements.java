package com.takeaway.game.dto;

import com.takeaway.game.model.Status;
import lombok.Builder;
import lombok.Getter;

import java.util.*;

@Builder
@Getter
public class GameMovements {

    private final UUID uuid;
    private final Status status;
    private final List<Move> movements;

}
