package com.takeaway.game.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional
public class Game implements Serializable {

    @Id
    UUID id;

    GameStatus status;
    String opponentId;
    GameMode mode;

    @OneToMany(targetEntity=Movement.class, cascade = ALL,fetch=FetchType.EAGER)
    List<Movement> movements;

}
