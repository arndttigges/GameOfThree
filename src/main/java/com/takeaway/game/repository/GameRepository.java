package com.takeaway.game.repository;

import com.takeaway.game.repository.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {

    Optional<Game> findByIdAndPlayerIdAndOpponentId(UUID id, String playerId, String opponentId);
}
