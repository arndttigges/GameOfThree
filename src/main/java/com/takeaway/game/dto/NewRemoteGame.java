package com.takeaway.game.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class NewRemoteGame {

    @Min(1)
    private int startValue;
    @NotBlank
    private String remotePlayer;

}
