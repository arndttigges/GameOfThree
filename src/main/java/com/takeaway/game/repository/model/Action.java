package com.takeaway.game.repository.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum Action {

    PLUS(1), ZERO(0), MINUS(-1);

    private final int value;
}
