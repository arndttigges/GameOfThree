package com.takeaway.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Getter
public class GameTemplate {

    @NotBlank
    private final String address;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private final int startValue;

}
