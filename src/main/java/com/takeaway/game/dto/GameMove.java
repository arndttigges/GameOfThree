package com.takeaway.game.dto;

import com.takeaway.game.model.Action;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Setter
@Getter
public class GameMove {
    @NotNull
    private Action action;
    private String playerId;
}
