package com.takeaway.game.service;

import com.takeaway.game.dto.*;
import com.takeaway.game.model.*;
import com.takeaway.game.repository.GameRepository;
import com.takeaway.game.rule.RuleEngine;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GameService {

    private final GameRepository repository;
    private final RuleEngine ruleEngine;

    public List<GameView> getAllRunningGames() {
        return repository.findAll()
                .stream()
                .map(game -> {
                    Movement lastMove = game.getMovements().get(game.getMovements().size() - 1);
                    return GameView.builder()
                            .uuid(game.getId())
                            .opponent(game.getOpponent().getId())
                            .lastStep(lastMove.getAction())
                            .sequenceNumber(lastMove.getMovementSequenzNumber())
                            .number(lastMove.getNumber())
                            .status(game.getStatus())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public DetailedGame fetchGame(UUID gameId) {
        Optional<Game> gameOptional = repository.findById(gameId);
        return gameOptional.map(this::convertGame).orElse(null);
    }

    public DetailedGame performMove(UUID gameId, GameMove action) {
        Game currentGame = repository.findById(gameId).orElseThrow();
        return performMove(currentGame, action);
    }

    public DetailedGame performMove(Game currentGame, GameMove action) {
        applyGameMove(currentGame, action);
        if(currentGame.getMode() == GameMode.LOCAL) {
            GameMove computerMove = createComputerMove();
            applyGameMove(currentGame, computerMove);
        } else {

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
        List<Move> moves = game.getMovements()
                .stream()
                .map(movement -> Move.builder()
                        .sequenceNumber(movement.getMovementSequenzNumber())
                        .number(movement.getNumber())
                        .opponentAction(movement.getPlayer().getId().equals(getSessionID()) ? null : movement.getAction())
                        .myAction(movement.getPlayer().getId().equals(getSessionID()) ? movement.getAction() : null)
                        .build())
                .collect(Collectors.toList());

        return DetailedGame.builder().uuid(game.getId()).status(game.getStatus()).movements(moves).build();
    }

    public Game createNewGame(GameTemplate gameTemplate) {
        Player player = Player.builder()
                .id(gameTemplate.getPlayerId())
                .address(gameTemplate.getAddress())
                .build();

        Movement startMove = Movement.builder()
                .movementSequenzNumber(1)
                .number(gameTemplate.getStartValue())
                .player(player)
                .build();

        Game newGame = Game.builder()
                .id(UUID.randomUUID())
                .mode(GameMode.LOCAL)
                .opponent(player)
                .movements(new LinkedList<>(List.of(startMove)))
                .build();
        applyGameMove(newGame, createComputerMove());
        return saveGame(newGame);
    }

    private Game applyMovementToGame(Game currentGame, Movement movement) {
        currentGame.getMovements().add(movement);
        return currentGame;
    }

    private GameStatus determineGameStatus(Game game) {
        Movement lastMovement = game.getMovements().get(game.getMovements().size() - 1);

        if(lastMovement.getNumber() <= 1) return GameStatus.FINISHED;
        if(lastMovement.getPlayer().getId().equals(getSessionID())) return GameStatus.WAITING;

        return GameStatus.READY;
    }

    private String getSessionID() {
        return RequestContextHolder.currentRequestAttributes().getSessionId();
    }

    private Game saveGame(Game game) {
        game.setStatus(determineGameStatus(game));
        return repository.save(game);
    }
}