package com.takeaway.game.kafka.dto;

import com.takeaway.game.model.Action;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class RemoteMove {

    @NotBlank
    private UUID gameId;

    private Action action;

    private int sequenceNumber;

    @NotEmpty
    private String playerId;


}
