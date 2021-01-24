package com.takeaway.game.controller;

import com.takeaway.game.dto.*;
import com.takeaway.game.kafka.KafkaService;
import com.takeaway.game.kafka.dto.Announcement;
import com.takeaway.game.kafka.dto.Invite;
import com.takeaway.game.kafka.dto.RemoteMove;
import com.takeaway.game.repository.model.Game;
import com.takeaway.game.repository.model.Mode;
import com.takeaway.game.repository.model.Movement;
import com.takeaway.game.repository.model.Status;
import com.takeaway.game.service.GameService;
import com.takeaway.game.service.InvitationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.RequestContextHolder;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class GameController {

    private final GameService gameService;
    private final InvitationService invitationService;
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
            gameService.createNewLocalGame(gameTemplate);
            model.addAttribute("gameTemplate", gameTemplate);
        }
        model.addAllAttributes(modelAttributesForMainPage());
        return "main";
    }

    @GetMapping("/game/remote")
    String announceAvailability(final Model model) {
        kafkaService.sendAnnouncement(new Announcement(getSessionId()));
        model.addAllAttributes(modelAttributesForMainPage());

        return "main";
    }

    @PostMapping("/game/remote/new")
    String announceAvailability(@ModelAttribute @Valid NewRemoteGame newRemoteGame,
                                final BindingResult bindingResult,
                                final Model model) {

        if (!bindingResult.hasErrors()) {
            Game game = gameService.createNewRemoteGame(UUID.randomUUID(), getSessionId(), newRemoteGame.getRemotePlayer(), getSessionId(), newRemoteGame.getStartValue());
            if (game != null) {
                invitationService.deleteInvitation(newRemoteGame.getRemotePlayer());
                Invite invite = new Invite(game.getId(), getSessionId(), newRemoteGame.getRemotePlayer(), game.getMovements().get(0).getNumber());
                kafkaService.sendInvite(invite);
            }
        }
        model.addAllAttributes(modelAttributesForMainPage());
        return "main";
    }

    @GetMapping("/game/{gameId}")
    String fetchGame(@PathVariable(name = "gameId") UUID gameId,
                     final Model model) {

        GameMovements game = gameService.fetchGame(gameId);
        model.addAllAttributes(modelAttributesForGamePage(game));

        return "game";
    }

    @PostMapping("/game/{gameId}")
    String play(@PathVariable(name = "gameId") UUID gameId,
                @ModelAttribute @Valid GameMove gameMove,
                final BindingResult bindingResult,
                final Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("game", gameService.fetchGame(gameId));
            model.addAttribute("playerId", getSessionId());
        } else {
            gameMove.setPlayerId(getSessionId());
            GameMovements gameMovements = gameService.performMove(gameId, gameMove);
            sendRemoteMove(gameMovements.getUuid());
            model.addAllAttributes(modelAttributesForGamePage(gameMovements));
        }

        return "game";
    }

    private void sendRemoteMove(UUID gameID) {
        gameService.getGameByUUID(gameID).ifPresent(game -> {
                    if (Mode.REMOTE == game.getMode()) {
                        Movement movement = game.getMovements().get(game.getMovements().size() - 1);

                        RemoteMove remoteMove = new RemoteMove(game.getId(),
                                movement.getAction(),
                                movement.getMovementSequenzNumber(),
                                getSessionId());
                        kafkaService.sendMove(remoteMove);
                    }
                }
        );
    }

    private Map<String, Object> modelAttributesForMainPage() {
        return Map.of(
                "playerId", getSessionId(),
                "gameTemplate", new GameTemplate(),
                "newRemoteGame", new NewRemoteGame(),
                "games", gameService.getAllRunningGames(),
                "players", invitationService.getInvitations()
        );
    }

    private Map<String, Object> modelAttributesForGamePage(GameMovements game) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("playerId", getSessionId());
        map.put("gameMove", new GameMove());
        map.put("game", game);

        if (game.getStatus() == Status.FINISHED) {
            Move move = game.getMovements().get(game.getMovements().size() - 1);
            map.put("winner", move.getMyAction() == null ? "Opponent" : "You");
        }

        return map;
    }

    private String getSessionId() {
        return RequestContextHolder.currentRequestAttributes().getSessionId();
    }

}
