package com.takeaway.game.repository.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movement {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private int movementSequenzNumber;
    private int number;
    private Action action;
    private String playerId;

}
