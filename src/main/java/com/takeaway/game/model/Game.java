package com.takeaway.game.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static javax.persistence.CascadeType.ALL;

@Entity
@Setter
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game implements Serializable {

    @Id
    UUID id;

    GameStatus status;
    GameMode mode;

    @OneToMany(targetEntity=Movement.class, cascade = ALL,fetch=FetchType.EAGER)
    List<Movement> movements;

    Player opponent;
}
