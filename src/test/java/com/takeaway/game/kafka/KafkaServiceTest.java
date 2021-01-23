package com.takeaway.game.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeaway.game.kafka.dto.Announcement;
import com.takeaway.game.kafka.dto.Invite;
import com.takeaway.game.kafka.dto.RemoteMove;
import com.takeaway.game.model.Action;
import com.takeaway.game.model.Game;
import com.takeaway.game.model.Invitation;
import com.takeaway.game.model.Mode;
import com.takeaway.game.repository.GameRepository;
import com.takeaway.game.repository.InvitationRepository;
import com.takeaway.game.rule.RuleEngine;
import com.takeaway.game.service.GameFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;
import java.util.UUID;

import static com.takeaway.game.kafka.KafkaService.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaServiceTest {

    private static final String REMOTE_PLAYER_ID = "REMOTE_PLAYER_ID";
    private static final String LOCAL_PLAYER_ID = "LOCAL_PLAYER_ID";
    private static final int START_VALUE = 42;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private RuleEngine ruleEngine;

    private final ObjectMapper mapper = new ObjectMapper();

    private KafkaService kafkaService;

    @BeforeEach
    void init() {
        kafkaService = new KafkaService(kafkaTemplate, invitationRepository, gameRepository, ruleEngine, mapper);
    }

    @Test
    void sendAnnouncementTest() {
        Announcement toSend = new Announcement(REMOTE_PLAYER_ID);
        kafkaService.sendAnnouncement(toSend);
        verify(kafkaTemplate).send(ANNOUNCEMENT_TOPIC, toSend);
    }

    @Test
    void sendInviteTest() {
        Invite toSend = new Invite(UUID.randomUUID(), LOCAL_PLAYER_ID, REMOTE_PLAYER_ID, START_VALUE);
        kafkaService.sendInvite(toSend);
        verify(kafkaTemplate).send(INVITE_GAME_TOPIC, toSend);
    }

    @Test
    void sendMoveTest() {
        RemoteMove toSend = new RemoteMove(UUID.randomUUID(), Action.ZERO, 2, LOCAL_PLAYER_ID);
        kafkaService.sendMove(toSend);
        verify(kafkaTemplate).send(MOVE_GAME_TOPIC, toSend);
    }

    @Test
    void saveReceivedAnnouncement() throws JsonProcessingException {
        kafkaService.listenToAnnouncements(mapper.writeValueAsString(new Announcement(REMOTE_PLAYER_ID)));
        verify(invitationRepository).save(any(Invitation.class));
    }

    @Test
    void doNotSaveInvalidAnnouncement() throws JsonProcessingException {
        kafkaService.listenToAnnouncements(mapper.writeValueAsString(new Announcement("")));
        verify(invitationRepository, times(0)).save(any(Invitation.class));
    }

    @Test
    void saveReceivedInvitation() throws JsonProcessingException {
        kafkaService.listenToInvites(mapper.writeValueAsString(new Invite(UUID.randomUUID(), LOCAL_PLAYER_ID, REMOTE_PLAYER_ID, START_VALUE)));
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void saveReceivedGameMove() throws JsonProcessingException {
        Game game = GameFactory.createNewGame(Mode.REMOTE, LOCAL_PLAYER_ID, REMOTE_PLAYER_ID, LOCAL_PLAYER_ID, START_VALUE);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        kafkaService.listenToMoves(mapper.writeValueAsString(new RemoteMove(game.getId(), Action.ZERO, 2, LOCAL_PLAYER_ID)));
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void doNotSaveReceivedGameMoveWhenSequenceNumberIsToLow() throws JsonProcessingException {
        Game game = GameFactory.createNewGame(Mode.REMOTE, LOCAL_PLAYER_ID, REMOTE_PLAYER_ID, LOCAL_PLAYER_ID, START_VALUE);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        kafkaService.listenToMoves(mapper.writeValueAsString(new RemoteMove(game.getId(), Action.ZERO, 1, LOCAL_PLAYER_ID)));
        verify(gameRepository, times(0)).save(any(Game.class));
    }

    @Test
    void doNotSaveReceivedGameMoveWhenGameNotSaved() throws JsonProcessingException {
        Game game = GameFactory.createNewGame(Mode.REMOTE, LOCAL_PLAYER_ID, REMOTE_PLAYER_ID, LOCAL_PLAYER_ID, START_VALUE);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.empty());
        kafkaService.listenToMoves(mapper.writeValueAsString(new RemoteMove(game.getId(), Action.ZERO, 1, LOCAL_PLAYER_ID)));
        verify(gameRepository, times(0)).save(any(Game.class));
    }

}