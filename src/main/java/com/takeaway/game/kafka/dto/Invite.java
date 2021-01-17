package com.takeaway.game.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@AllArgsConstructor
@Getter
@ToString
public class Invite {

    @NotBlank
    private final UUID gameId;
    @NotBlank
    private final String playerId;
    private final String name;
    @Min(1)
    private final int startValue;

}
