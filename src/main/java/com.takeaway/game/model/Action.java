package com.takeaway.game.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Action {

    PLUS(1), ZERO(0), MINUS(-1);

    private final int value;
}
