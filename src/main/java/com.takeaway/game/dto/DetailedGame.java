package com.takeaway.game.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.*;

@Builder
@Getter
public class DetailedGame {

    List<Move> movements;

}
