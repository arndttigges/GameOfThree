package com.takeaway.game.dto;

import com.takeaway.game.model.Action;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GameMove {
    private Action move;
    @NotEmpty
    private String playerId;
}
