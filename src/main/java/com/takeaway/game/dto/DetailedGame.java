package com.takeaway.game.dto;

import com.takeaway.game.model.GameStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.*;

@Builder
@Getter
public class DetailedGame {

    private final UUID uuid;
    private final GameStatus status;
    private final List<Move> movements;

}
