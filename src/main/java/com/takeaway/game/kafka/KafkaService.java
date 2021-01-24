package com.takeaway.game.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeaway.game.dto.GameMove;
import com.takeaway.game.kafka.dto.Announcement;
import com.takeaway.game.kafka.dto.Invite;
import com.takeaway.game.kafka.dto.RemoteMove;
import com.takeaway.game.repository.InvitationRepository;
import com.takeaway.game.repository.model.Invitation;
import com.takeaway.game.rule.RuleEngine;
import com.takeaway.game.service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaService {

    static final String ANNOUNCEMENT_TOPIC = "READY";
    static final String INVITE_GAME_TOPIC = "INVITE";
    static final String MOVE_GAME_TOPIC = "MOVE";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final InvitationRepository invitationRepository;
    private final GameService gameService;
    private final RuleEngine ruleEngine;

    private final ObjectMapper mapper;

    public void sendAnnouncement(Announcement announcement) {
        kafkaTemplate.send(ANNOUNCEMENT_TOPIC, announcement);
    }

    public void sendInvite(Invite invite) {
        kafkaTemplate.send(INVITE_GAME_TOPIC, invite);
    }

    public void sendMove(RemoteMove remoteMove) {
        kafkaTemplate.send(MOVE_GAME_TOPIC, remoteMove);
    }

    @KafkaListener(topics = ANNOUNCEMENT_TOPIC, groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void listenToAnnouncements(String announcementString) throws JsonProcessingException {
        Announcement announcement = mapper.readValue(announcementString, Announcement.class);
        if (!announcement.getPlayerId().isEmpty()) {
            invitationRepository.save(new Invitation(announcement.getPlayerId()));
        }
    }

    @KafkaListener(topics = "INVITE", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void listenToInvites(String inviteString) throws JsonProcessingException {
        Invite invite = mapper.readValue(inviteString, Invite.class);
        gameService.createNewRemoteGame(invite.getGameId(), invite.getOpponentId(), invite.getPlayerId(), invite.getPlayerId(), invite.getStartValue());
    }

    @KafkaListener(topics = "MOVE", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void listenToMoves(String moveString) throws JsonProcessingException {
        RemoteMove move = mapper.readValue(moveString, RemoteMove.class);

        GameMove gameMove = new GameMove();
        gameMove.setAction(move.getAction());
        gameMove.setPlayerId(move.getPlayerId());
        gameService.performRemoteMove(move.getGameId(), move.getSequenceNumber(), gameMove);
    }
}
