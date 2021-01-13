package com.takeaway.game.controller;

import com.takeaway.game.dto.GameTemplate;
import com.takeaway.game.service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@AllArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/")
    String getStartPage(Model model) {
        model.addAttribute("games", gameService.getAllRunningGames());
        return "main";
    }

    @PostMapping("/game/new")
    String startNewGame(@Valid @ModelAttribute GameTemplate gameTemplate,
                        final BindingResult bindingResult,
                        final Model model ) {

        if (!bindingResult.hasErrors()) {
           gameService.createNewGame(gameTemplate);
        }

        return "main";
    }

}
