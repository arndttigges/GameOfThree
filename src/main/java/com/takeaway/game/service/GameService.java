package com.takeaway.game.service;

import com.takeaway.game.dto.*;
import com.takeaway.game.kafka.KafkaService;
import com.takeaway.game.model.*;
import com.takeaway.game.repository.GameRepository;
import com.takeaway.game.rule.RuleEngine;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.network.Mode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerService playerService;
    private final KafkaService kafkaService;
    private final RuleEngine ruleEngine;

    public List<GameView> getAllRunningGames() {
        return gameRepository.findAll()
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
        Optional<Game> gameOptional = gameRepository.findById(gameId);
        return gameOptional.map(this::convertGame).orElse(null);
    }

    public DetailedGame performMove(UUID gameId, GameMove action) {
        Game currentGame = gameRepository.findById(gameId).orElseThrow();
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
                        .opponentAction(movement.getPlayer() == null ? null :movement.getPlayer().getId().equals(getSessionID()) ? null : movement.getAction())
                        .myAction(movement.getPlayer() == null ? null :movement.getPlayer().getId().equals(getSessionID()) ? movement.getAction() : null)
                        .build())
                .collect(Collectors.toList());

        return DetailedGame.builder().uuid(game.getId()).status(game.getStatus()).movements(moves).build();
    }

    public Game createNewLocalGame(GameTemplate gameTemplate) {

        Player player = playerService.createPlayer(gameTemplate.getPlayerId(), gameTemplate.getName());
        List<Movement> startMovementList = initMovementList(gameTemplate.getStartValue(), player);

        Game newGame = createGameEntity(player, startMovementList, GameMode.LOCAL);
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
        return gameRepository.save(game);
    }

    public void createNewRemoteGame(NewRemoteGame newRemoteGame) {
        int startValue = newRemoteGame.getStartValue();
        playerService.findPlayerById(newRemoteGame.getRemotePlayer()).ifPresent(remotePlayer -> {
            String playerId = getSessionID();
            Player player = playerService.createPlayer(playerId, playerId);
            List<Movement> startMovement = initMovementList(startValue, player);

            Game remoteGame = gameRepository.save(createGameEntity(remotePlayer, startMovement, GameMode.REMOTE));

            kafkaService.sendInvite(remoteGame.getId(), playerId, playerId, startValue);
        });


    }

    private List<Movement> initMovementList(int startValue, Player firstPlayer){
        Movement startMove = Movement.builder()
                .movementSequenzNumber(1)
                .number(startValue)
                .player(firstPlayer)
                .build();

        return new LinkedList(List.of(startMove));
    }

    private Game createGameEntity(Player player, List<Movement> movements, GameMode mode) {
        return Game.builder()
                .id(UUID.randomUUID())
                .mode(mode)
                .opponent(player)
                .movements(movements)
                .build();
    }
}