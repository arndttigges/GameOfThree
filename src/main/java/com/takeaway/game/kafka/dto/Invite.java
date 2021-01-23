package com.takeaway.game.kafka.dto;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Invite {

    @NotBlank
    private UUID gameId;
    @NotBlank
    private String playerId;
    @NotBlank
    private String opponentId;
    @Min(1)
    private int startValue;

}
