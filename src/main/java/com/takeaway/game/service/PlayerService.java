package com.takeaway.game.service;

import com.takeaway.game.kafka.KafkaService;
import com.takeaway.game.kafka.dto.Announcement;
import com.takeaway.game.model.GameStatus;
import com.takeaway.game.model.Player;
import com.takeaway.game.repository.PlayerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public List<Player> getAllPlayerReadyToPlay() {
        return playerRepository.findAll().stream()
                .filter(player -> !player.getId().equals(getSessionID()))
                .filter(player -> player.getGames() == null || player.getGames().isEmpty() ||
                        player.getGames().stream()
                        .anyMatch(game -> game.getStatus() == GameStatus.FINISHED)
                )
                .collect(Collectors.toList());
    }

    public Optional<Player> findPlayerById(String id) {
        return playerRepository.findById(id);
    }

    public Player createPlayer(String playerId, String name) {
        if(!StringUtils.hasText(playerId)) {
            return null;
        }

        Player player = Player.builder()
                .id(playerId)
                .name(name)
                .build();
        return playerRepository.save(player);
    }

    private String getSessionID() {
        return RequestContextHolder.currentRequestAttributes().getSessionId();
    }
}
