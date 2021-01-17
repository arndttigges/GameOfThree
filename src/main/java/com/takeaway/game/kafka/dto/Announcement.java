package com.takeaway.game.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Getter
@ToString
public class Announcement {

    @NotBlank
    private final String playerId;
    private final String name;

}
