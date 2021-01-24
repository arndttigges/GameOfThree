package com.takeaway.game.service;

import com.takeaway.game.dto.DetailedGameView;
import com.takeaway.game.dto.GameMove;
import com.takeaway.game.dto.GameOverviewElement;
import com.takeaway.game.dto.GameTemplate;
import com.takeaway.game.repository.GameRepository;
import com.takeaway.game.repository.model.Action;
import com.takeaway.game.repository.model.Game;
import com.takeaway.game.repository.model.Mode;
import com.takeaway.game.repository.model.Movement;
import com.takeaway.game.rule.RuleEngine;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.*;

@Service
@AllArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final RuleEngine ruleEngine;

    public List<GameOverviewElement> getAllRunningGames() {

        List<Game> games = gameRepository.findAll();
        List<Game> filteredAndValidGames = filterAndDeleteInvalidGames(games);
        return GameFactory.convertToGameView(filteredAndValidGames, getSessionId());
    }

    private List<Game> filterAndDeleteInvalidGames(List<Game> games) {
        List<Game> myGames = new ArrayList<>();
        List<Game> invalidGames = new ArrayList<>();

        String me = getSessionId();
        for (Game game : games) {
            if (me.equals(game.getPlayerId()) || me.equals(game.getOpponentId())) {
                myGames.add(game);
            } else {
                invalidGames.add(game);
            }
        }
        gameRepository.deleteAll(invalidGames);
        return myGames;
    }

    public Optional<Game> getGameByUUID(UUID gameId) {
        return gameRepository.findById(gameId);
    }

    public DetailedGameView fetchGame(UUID gameId) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);
        return gameOptional.map(this::convertGame).orElse(null);
    }

    public DetailedGameView performMove(UUID gameId, GameMove action) {
        Game currentGame = gameRepository.findById(gameId).orElseThrow();
        return performMove(currentGame, action);
    }

    public DetailedGameView performMove(Game currentGame, GameMove action) {
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

    private DetailedGameView convertGame(Game game) {
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

    public Game createNewRemoteGame(UUID gameID, String lokalPlayer, String remotePlayer, String initiator, int value) {
        Game initRemoteGame = GameFactory.createNewGame(Mode.REMOTE, lokalPlayer, remotePlayer, initiator, value);
        initRemoteGame.setId(gameID);
        return gameRepository.save(initRemoteGame);
    }

    public void performRemoteMove(UUID gameId, int sequenceNumber, GameMove gameMove) {
        gameRepository.findById(gameId)
                .ifPresent(game -> {
                    if (game.getMovements().get(game.getMovements().size() - 1).getMovementSequenzNumber() != sequenceNumber) {
                        applyGameMove(game, gameMove);
                        gameRepository.save(game);
                    }
                });
    }
}