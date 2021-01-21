package com.takeaway.game.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeaway.game.dto.GameMove;
import com.takeaway.game.kafka.dto.Announcement;
import com.takeaway.game.kafka.dto.Invite;
import com.takeaway.game.kafka.dto.RemoteMove;
import com.takeaway.game.model.Action;
import com.takeaway.game.model.Game;
import com.takeaway.game.model.GameMode;
import com.takeaway.game.model.Invitation;
import com.takeaway.game.repository.GameRepository;
import com.takeaway.game.repository.InvitationRepository;
import com.takeaway.game.rule.RuleEngine;
import com.takeaway.game.service.GameFactory;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class KafkaService {

    private static final String READY_FOR_GAME_TOPIC = "READY";
    private static final String INVITE_GAME_TOPIC = "INVITE";
    private static final String MOVE_GAME_TOPIC = "MOVE";

    private final KafkaTemplate<String, Announcement> announcementTemplate;
    private final KafkaTemplate<String, Invite> inviteKafkaTemplate;
    private final KafkaTemplate<String, RemoteMove> moveKafkaTemplate;

    private final InvitationRepository invitationRepository;
    private final GameRepository gameRepository;
    private final RuleEngine ruleEngine;

    private final ObjectMapper mapper;

    public void sendAnnouncement(Announcement announcement) {
        announcementTemplate.send(READY_FOR_GAME_TOPIC, announcement);
    }

    @KafkaListener(topics = READY_FOR_GAME_TOPIC, groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void listenToAnnouncements(String announcementString) throws JsonProcessingException {
        Announcement announcement = mapper.readValue(announcementString, Announcement.class);
        if (!announcement.getPlayerId().isEmpty()) {
            invitationRepository.save(new Invitation(announcement.getPlayerId()));
        }
    }

    public void sendInvite(UUID id, String playerId, String name, int startValue) {
        Invite invite = new Invite(id, playerId, name, startValue);
        inviteKafkaTemplate.send(INVITE_GAME_TOPIC, invite);
    }

    @KafkaListener(topics = "INVITE", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void listenToInvites(String inviteString) throws JsonProcessingException {
        Invite invite = mapper.readValue(inviteString, Invite.class);

        Game game = GameFactory.createNewGame(GameMode.REMOTE, invite.getPlayerId(), invite.getPlayerId(), invite.getStartValue());
        game.setId(invite.getGameId());
        gameRepository.save(game);
        System.out.println("Received Message in group: " + invite);
    }

    public void sendMove(UUID gameID, String playerId, Action action) {
        RemoteMove remoteMove = new RemoteMove(gameID, action, playerId);
        moveKafkaTemplate.send(MOVE_GAME_TOPIC, remoteMove);
    }

    @KafkaListener(topics = "MOVE", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void listenToMoves(String moveString) throws JsonProcessingException {
        RemoteMove move = mapper.readValue(moveString, RemoteMove.class);

        gameRepository.findById(move.getGameId()).ifPresent(game -> {
            GameMove gameMove = new GameMove(move.getAction(), move.getPlayerId());
            ruleEngine.executeMove(game, gameMove).ifPresent(
                    movement -> game.getMovements().add(movement)
            );
            gameRepository.save(game);
        });
    }
}
