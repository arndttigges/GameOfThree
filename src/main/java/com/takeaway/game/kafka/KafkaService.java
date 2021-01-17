package com.takeaway.game.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeaway.game.dto.GameMove;
import com.takeaway.game.kafka.dto.Announcement;
import com.takeaway.game.kafka.dto.Invite;
import com.takeaway.game.kafka.dto.RemoteMove;
import com.takeaway.game.model.Player;
import com.takeaway.game.repository.PlayerRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

@Service
@AllArgsConstructor
public class KafkaService {

    private static final String READY_FOR_GAME_TOPIC = "READY";
    private static final String INVITE_GAME_TOPIC = "INVITE";
    private static final String MOVE_GAME_TOPIC = "MOVE";

    private final KafkaTemplate<String, Announcement> announcementTemplate;
    private final KafkaTemplate<String, Invite> inviteKafkaTemplate;
    private final KafkaTemplate<String, RemoteMove> moveKafkaTemplate;

    private final PlayerRepository playerRepository;

    private final ObjectMapper mapper;

    public void sendAnnouncement(Announcement announcement) {
        announcementTemplate.send(READY_FOR_GAME_TOPIC, announcement);
    }

    @SneakyThrows
    @KafkaListener(topics = READY_FOR_GAME_TOPIC, groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void listenToAnnouncements(String announcementString) {
        Announcement announcement = mapper.readValue(announcementString, Announcement.class);
        if(!announcement.getPlayerId().isEmpty()) {
            Player player = Player.builder()
                    .id(announcement.getPlayerId())
                    .name(announcement.getName())
                    .build();
            playerRepository.save(player);
        }
    }

    @SneakyThrows
    @KafkaListener(topics = "INVITE", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void listenToInvites(String inviteString) {
        Invite invite = mapper.readValue(inviteString, Invite.class);
        System.out.println("Received Message in group: " + invite);
    }

    @SneakyThrows
    @KafkaListener(topics = "MOVE", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void listenToMoves(String moveString) {
        RemoteMove move = mapper.readValue(moveString, RemoteMove.class);
        System.out.println("Received Message in group: " + move);
    }
}
