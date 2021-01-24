package com.takeaway.game.repository.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static javax.persistence.CascadeType.ALL;

@Entity
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game implements Serializable {
    @Id
    UUID id;

    @NotEmpty
    String playerId;
    @NotEmpty
    String opponentId;
    Mode mode;

    @OneToMany(targetEntity=Movement.class, cascade = ALL,fetch=FetchType.EAGER)
    List<Movement> movements;

}
