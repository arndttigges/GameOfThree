package com.takeaway.game.controller;

import com.takeaway.game.dto.GameMove;
import com.takeaway.game.dto.GameTemplate;
import com.takeaway.game.dto.NewRemoteGame;
import com.takeaway.game.kafka.KafkaService;
import com.takeaway.game.kafka.dto.Announcement;
import com.takeaway.game.model.Game;
import com.takeaway.game.service.GameService;
import com.takeaway.game.service.PlayerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class GameController {

    private final GameService gameService;
    private final PlayerService playerService;
    private final KafkaService kafkaService;

    @GetMapping("/")
    String getStartPage(Model model) {
        model.addAllAttributes(modelAttributesForMainPage());
        return "main";
    }

    @PostMapping("/game/new")
    String startNewGame(@Valid @ModelAttribute GameTemplate gameTemplate,
                        final BindingResult bindingResult,
                        final Model model) {

        if (!bindingResult.hasErrors()) {
            Game createdGame = gameService.createNewLocalGame(gameTemplate);
            Announcement announcement = new Announcement(gameTemplate.getPlayerId(), gameTemplate.getName());
            kafkaService.sendAnnouncement(announcement);
            model.addAttribute("gameTemplate", gameTemplate);
        }
        model.addAllAttributes(modelAttributesForMainPage());
        return "main";
    }

    @GetMapping("/game/remote")
    String announceAvailability(final HttpSession session,
                                final Model model) {

        playerService.createReadyToPlayAnnouncement(session.getId());
        model.addAllAttributes(modelAttributesForMainPage());
        return "main";
    }

    @PostMapping("/game/remote/new")
    String announceAvailability(@ModelAttribute @Valid NewRemoteGame newRemoteGame,
                                final BindingResult bindingResult,
                                final Model model) {

        if (!bindingResult.hasErrors()) {
            gameService.createNewRemoteGame(newRemoteGame);
        }
        model.addAllAttributes(modelAttributesForMainPage());
        return "main";
    }

    @GetMapping("/game/{gameId}")
    String fetchGame(@PathVariable(name = "gameId") UUID gameId,
                     final HttpSession session,
                     final Model model) {
        model.addAttribute("game", gameService.fetchGame(gameId));
        model.addAttribute("playerId", session.getId());
        model.addAttribute("gameMove", new GameMove());
        return "game";
    }


    @PostMapping("/game/{gameId}")
    String play(@PathVariable(name = "gameId") UUID gameId,
                @ModelAttribute @Valid GameMove gameMove,
                final BindingResult bindingResult,
                final HttpSession session,
                final Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("game", gameService.fetchGame(gameId));
            model.addAttribute("playerId", session.getId());
        } else {
            model.addAttribute("game", gameService.performMove(gameId, gameMove));
            model.addAttribute("playerId", session.getId());
            model.addAttribute("gameMove", new GameMove());
        }

        return "game";
    }

    private Map<String, Object> modelAttributesForMainPage() {
        return Map.of(
                "playerId", RequestContextHolder.currentRequestAttributes().getSessionId(),
                "gameTemplate", new GameTemplate(),
                "newRemoteGame", new NewRemoteGame(),
                "games", gameService.getAllRunningGames(),
                "players", playerService.getAllInvites()
        );
    }

}
