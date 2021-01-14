package com.takeaway.game.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.*;

@Builder
@Getter
public class DetailedGame {

    private final UUID uuid;
    private final List<Move> movements;

}
