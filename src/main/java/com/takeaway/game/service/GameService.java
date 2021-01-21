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
    private final KafkaService kafkaService;
    private final RuleEngine ruleEngine;

    public List<GameOverviewElement> getAllRunningGames() {
        return GameFactory.convertToGameView(gameRepository.findAll());
    }

    public GameMovements fetchGame(UUID gameId) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);
        return gameOptional.map(this::convertGame).orElse(null);
    }

    public GameMovements performMove(UUID gameId, GameMove action) {
        Game currentGame = gameRepository.findById(gameId).orElseThrow();
        return performMove(currentGame, action);
    }

    public GameMovements performMove(Game currentGame, GameMove action) {
        applyGameMove(currentGame, action);
        if (currentGame.getMode() == Mode.LOCAL) {
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

    private GameMovements convertGame(Game game) {
        return GameFactory.createDetailedGameFromGame(game, getSessionID());
    }

    public Game createNewLocalGame(GameTemplate gameTemplate) {
        Game localGame = GameFactory.createNewGame(Mode.LOCAL, "COMPUTER", gameTemplate.getPlayerId(), gameTemplate.getStartValue());
        applyGameMove(localGame, createComputerMove());
        return saveGame(localGame);
    }

    private Game applyMovementToGame(Game currentGame, Movement movement) {
        currentGame.getMovements().add(movement);
        return currentGame;
    }

    Status determineGameStatus(Game game) {
        Movement lastMovement = game.getMovements().get(game.getMovements().size() - 1);

        if (lastMovement.getNumber() <= 1) return Status.FINISHED;
        if (lastMovement.getPlayerId().equals(getSessionID())) return Status.WAITING;

        return Status.READY;
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
        Game initRemoteGame = GameFactory.createNewGame(Mode.REMOTE, newRemoteGame.getRemotePlayer(), getSessionID(), startValue);
        return gameRepository.save(initRemoteGame);
    }
}