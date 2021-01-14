package com.takeaway.game.controller;

import com.takeaway.game.dto.GameMove;
import com.takeaway.game.dto.GameTemplate;
import com.takeaway.game.model.Action;
import com.takeaway.game.model.Game;
import com.takeaway.game.service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/")
    String getStartPage(Model model, HttpSession session) {
        model.addAttribute("gameTemplate", new GameTemplate());
        model.addAttribute("playerId", session.getId());
        model.addAttribute("games", gameService.getAllRunningGames());
        return "main";
    }

    @PostMapping("/game/new")
    String startNewGame(@Valid @ModelAttribute GameTemplate gameTemplate,
                        final BindingResult bindingResult,
                        final Model model) {

        if (!bindingResult.hasErrors()) {
            Game createdGame = gameService.createNewGame(gameTemplate);
        }

        model.addAttribute("games", gameService.getAllRunningGames());
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
                @ModelAttribute GameMove gameMove,
                final Model model) {

        model.addAttribute("game", gameService.performMove(gameId, gameMove));
        model.addAttribute("gameMove", new GameMove());
        return "game";
    }


}
