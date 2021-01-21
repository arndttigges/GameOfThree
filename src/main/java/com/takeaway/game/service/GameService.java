package com.takeaway.game.service;

import com.takeaway.game.dto.*;
import com.takeaway.game.kafka.KafkaService;
import com.takeaway.game.model.*;
import com.takeaway.game.repository.GameRepository;
import com.takeaway.game.rule.RuleEngine;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final InvitationService playerService;
    private final KafkaService kafkaService;
    private final RuleEngine ruleEngine;

    public List<GameView> getAllRunningGames() {
        return GameFactory.convertToGameView(gameRepository.findAll());
    }

    public DetailedGame fetchGame(UUID gameId) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);
        return gameOptional.map(this::convertGame).orElse(null);
    }

    public DetailedGame performMove(UUID gameId, GameMove action) {
        Game currentGame = gameRepository.findById(gameId).orElseThrow();
        return performMove(currentGame, action);
    }

    public DetailedGame performMove(Game currentGame, GameMove action) {
        applyGameMove(currentGame, action);
        if (currentGame.getMode() == GameMode.LOCAL) {
            GameMove computerMove = createComputerMove();
            applyGameMove(currentGame, computerMove);
        } else {
            kafkaService.sendMove(currentGame.getId(), action.getPlayerId(), action.getMove());
        }
        return convertGame(saveGame(currentGame));
    }

    private void applyGameMove(Game currentGame, GameMove action) {
        ruleEngine.executeMove(currentGame, action)
                .ifPresent(move -> applyMovementToGame(currentGame, move));
    }

    private GameMove createComputerMove() {
        Action computerAction = Arrays.stream(Action.values()).findAny().orElse(Action.ZERO);
        return new GameMove(computerAction, "COMPUTER");
    }

    private DetailedGame convertGame(Game game) {
        return GameFactory.createDetailedGameFromGame(game, getSessionID());
    }

    public Game createNewLocalGame(GameTemplate gameTemplate) {


        Game localGame = GameFactory.createNewGame(GameMode.LOCAL, "COMPUTER", gameTemplate.getPlayerId(), gameTemplate.getStartValue());
        applyGameMove(localGame, createComputerMove());
        return saveGame(localGame);
    }

    private Game applyMovementToGame(Game currentGame, Movement movement) {
        currentGame.getMovements().add(movement);
        return currentGame;
    }

    private GameStatus determineGameStatus(Game game) {
        Movement lastMovement = game.getMovements().get(game.getMovements().size() - 1);

        if (lastMovement.getNumber() <= 1) return GameStatus.FINISHED;
        if (lastMovement.getPlayerId().equals(getSessionID())) return GameStatus.WAITING;

        return GameStatus.READY;
    }

    private String getSessionID() {
        return RequestContextHolder.currentRequestAttributes().getSessionId();
    }

    private Game saveGame(Game game) {
        game.setStatus(determineGameStatus(game));
        return gameRepository.save(game);
    }

    public Game createNewRemoteGame(NewRemoteGame newRemoteGame) {
        int startValue = newRemoteGame.getStartValue();
        Game initRemoteGame = GameFactory.createNewGame(GameMode.REMOTE, newRemoteGame.getRemotePlayer(), getSessionID(), startValue);
        return gameRepository.save(initRemoteGame);
    }
}