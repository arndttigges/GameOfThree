package com.takeaway.game.service;

import com.takeaway.game.dto.*;
import com.takeaway.game.model.Action;
import com.takeaway.game.model.Game;
import com.takeaway.game.model.Mode;
import com.takeaway.game.model.Movement;
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
    private final RuleEngine ruleEngine;

    public List<GameOverviewElement> getAllRunningGames() {
        return GameFactory.convertToGameView(gameRepository.findAll(), getSessionId());
    }

    public Optional<Game> getGameByUUID(UUID gameId) {
        return gameRepository.findById(gameId);
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
        }
        return convertGame(gameRepository.save(currentGame));
    }


    private void applyGameMove(Game currentGame, GameMove action) {
        ruleEngine.executeMove(currentGame, action)
                .ifPresent(move -> applyMovementToGame(currentGame, move));
    }

    private GameMove createComputerMove() {
        Action computerAction = Arrays.stream(Action.values()).findAny().orElse(Action.ZERO);
        GameMove move = new GameMove();
        move.setAction(computerAction);
        move.setPlayerId("COMPUTER");
        return move;
    }

    private GameMovements convertGame(Game game) {
        return GameFactory.createDetailedGameFromGame(game, getSessionId());
    }

    public Game createNewLocalGame(GameTemplate gameTemplate) {
        Game localGame = GameFactory.createNewGame(Mode.LOCAL, getSessionId(), "COMPUTER", getSessionId(), gameTemplate.getStartValue());
        applyGameMove(localGame, createComputerMove());
        return gameRepository.save(localGame);
    }

    private Game applyMovementToGame(Game currentGame, Movement movement) {
        currentGame.getMovements().add(movement);
        return currentGame;
    }

    private String getSessionId() {
        return RequestContextHolder.currentRequestAttributes().getSessionId();
    }

    public Game createNewRemoteGame(NewRemoteGame newRemoteGame) {
        int startValue = newRemoteGame.getStartValue();
        Game initRemoteGame = GameFactory.createNewGame(Mode.REMOTE, getSessionId(), newRemoteGame.getRemotePlayer(), getSessionId(), startValue);
        return gameRepository.save(initRemoteGame);
    }
}