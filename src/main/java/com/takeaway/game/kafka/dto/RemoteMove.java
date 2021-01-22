package com.takeaway.game.kafka.dto;

import com.takeaway.game.model.Action;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@AllArgsConstructor
@Getter
@ToString
public class RemoteMove {

    @NotBlank
    private final UUID gameId;

    private Action action;

    private int sequenznumber;

    @NotEmpty
    private String playerId;

    @NotEmpty
    private String opponentId;

}
