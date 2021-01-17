package com.takeaway.game.service;

import com.takeaway.game.kafka.KafkaService;
import com.takeaway.game.kafka.dto.Announcement;
import com.takeaway.game.model.Player;
import com.takeaway.game.repository.PlayerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final KafkaService kafkaService;

    public List<Player> getAllInvites() {
        return playerRepository.findAll().stream()
                .filter(player -> !player.getId().equals(getSessionID()))
                .collect(Collectors.toList());
    }

    public void createReadyToPlayAnnouncement(String name) {
        Announcement announcement = new Announcement(getSessionID(),name);
        kafkaService.sendAnnouncement(announcement);
    }

    public Optional<Player> findPlayerById(String id) {
        return playerRepository.findById(id);
    }

    public Player createPlayer(String playerId, String name) {
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
