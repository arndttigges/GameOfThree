package com.takeaway.game.controller;

import com.takeaway.game.model.Action;
import com.takeaway.game.model.Game;
import com.takeaway.game.model.Mode;
import com.takeaway.game.repository.GameRepository;
import com.takeaway.game.service.GameFactory;
import com.takeaway.game.service.GameService;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    GameRepository gameRepository;

    @Test
    public void shouldReturnDefaultMessage() throws Exception {
        this.mockMvc.perform(get("/")).andExpect(status().isOk());
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
}