package com.takeaway.game.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@AllArgsConstructor
@Getter
@ToString
public class RemoteMove {

    @NotBlank
    private final UUID gameId;

}
