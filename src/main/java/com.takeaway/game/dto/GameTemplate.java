package com.takeaway.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GameTemplate {

    @NotBlank
    private String address = "xxx";

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int startValue = 42;

}
