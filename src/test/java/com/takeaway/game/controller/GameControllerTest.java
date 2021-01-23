package com.takeaway.game.controller;

import com.takeaway.game.kafka.KafkaService;
import com.takeaway.game.kafka.dto.Announcement;
import com.takeaway.game.model.Action;
import com.takeaway.game.model.Game;
import com.takeaway.game.model.Mode;
import com.takeaway.game.repository.GameRepository;
import com.takeaway.game.service.GameFactory;
import com.takeaway.game.service.GameService;
import com.takeaway.game.service.InvitationService;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class GameControllerTest {

    @MockBean
    KafkaService kafkaService;

    @MockBean
    InvitationService invitationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    GameRepository gameRepository;

    @BeforeEach
    void init() {
        gameRepository.deleteAll();
    }

    @Test
    public void getStartPageWithoutErrors() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    public void senAnnouncement() throws Exception {
        mockMvc.perform(get("/game/remote")).andExpect(status().isOk());
        verify(kafkaService).sendAnnouncement(any(Announcement.class));
    }

    @Test
    public void createANewLocalGame() throws Exception{
        mockMvc.perform(
                post("/game/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Collections.singletonList(
                                new BasicNameValuePair("startValue", "42")
                        ))))
        ).andExpect(status().isOk());

        assertEquals(1, gameRepository.findAll().size());
        verify(kafkaService, times(0)).sendAnnouncement(any(Announcement.class));
    }

    @Test
    public void createNewRemoteGame() throws Exception{
        String remotePlayerId = "REMOTE_PLAYER_ID";
        int startValue = 42;
        mockMvc.perform(
                post("/game/remote/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
                                new BasicNameValuePair("startValue", "" + startValue ),
                                new BasicNameValuePair("remotePlayer", remotePlayerId)
                        ))))
        ).andExpect(status().isOk());

        assertEquals(1, gameRepository.findAll().size());
        verify(kafkaService).sendInvite(any(UUID.class), anyString(), eq(remotePlayerId), eq(startValue));
        verify(invitationService).deleteInvitation(remotePlayerId);
    }

    @Test
    public void doNotCreateNewRemoteGameWhenInputIsInvalid() throws Exception{
        mockMvc.perform(
                post("/game/remote/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        ).andExpect(status().isOk());

        assertEquals(0, gameRepository.findAll().size());
        verify(kafkaService, times(0)).sendInvite(any(UUID.class), anyString(), anyString(), anyInt());
        verify(invitationService, times(0)).deleteInvitation(anyString());
    }

    @Test
    public void getGamePage() throws Exception {
        Game game = GameFactory.createNewGame(Mode.REMOTE, "A", "B", "A", 42);

        gameRepository.save(game);

        mockMvc.perform(get("/game/" + game.getId())).andExpect(status().isOk());
    }

    @Test
    public void doingAMove() throws Exception{
        Game init = GameFactory.createNewGame(
                Mode.REMOTE, "LOKAL", "REMOTE", "REMOTE", 42);
        Game testGame = gameRepository.save(init);

        String url = "/game/"+ testGame.getId().toString();
        this.mockMvc.perform(
                post(url)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                                new BasicNameValuePair("action", Action.ZERO.name()),
                                new BasicNameValuePair("playerId", "test")
                        ))))
        ).andExpect(status().isOk());
    }

    @Test
    public void preventToDoInvalidMove() throws Exception{
        Game init = GameFactory.createNewGame(
                Mode.REMOTE, "LOKAL", "REMOTE", "REMOTE", 42);
        Game testGame = gameRepository.save(init);

        String url = "/game/"+ testGame.getId().toString();
        this.mockMvc.perform(
                post(url)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Collections.singletonList(
                                new BasicNameValuePair("action", Action.ZERO.name())
                        ))))
        ).andExpect(status().isOk());
    }
}