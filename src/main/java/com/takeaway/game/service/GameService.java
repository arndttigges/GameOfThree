package com.takeaway.game.service;

import com.takeaway.game.dto.*;
import com.takeaway.game.model.Action;
import com.takeaway.game.model.Game;
import com.takeaway.game.model.Movement;
import com.takeaway.game.model.Player;
import com.takeaway.game.repository.GameRepository;
import com.takeaway.game.rule.Rule;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GameService {

    private final GameRepository repository;
    private final Rule rule;

    public List<GameView> getAllRunningGames() {
        return repository.findAll()
                .stream()
                .map(game -> {
                    Movement lastMove = game.getMovements().get(game.getMovements().size() - 1);
                    return GameView.builder()
                            .uuid(game.getId())
                            .opponentAddress(game.getOpponent().getAddress())
                            .lastStep(lastMove.getAction())
                            .sequenceNumber(lastMove.getMovementSequenzNumber())
                            .number(lastMove.getNumber())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public DetailedGame fetchGame(UUID gameId) {
        Optional<Game> gameOptional = repository.findById(gameId);
        return gameOptional.map(this::convertGame).orElse(null);
    }

    public DetailedGame performMove(UUID gameId, GameMove action) {
        Optional<Game> gameOptional = repository.findById(gameId);
        if( gameOptional.isPresent() ) {
            Game game = gameOptional.get();
            Movement move = rule.apply(game,action);
            game.getMovements().add(move);
            return convertGame(repository.save(game));
        }

        return fetchGame(gameId);
    }

    private DetailedGame convertGame(Game game) {
        List<Move> moves = game.getMovements()
                .stream()
                .map(movement -> Move.builder()
                        .sequenceNumber(movement.getMovementSequenzNumber())
                        .number(movement.getNumber())
                        .myAction(movement.getAction())
                        .build())
                .collect(Collectors.toList());

        return DetailedGame.builder().uuid(game.getId()).movements(moves).build();
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
                .opponent(player)
                .movements(List.of(startMove))
                .build();
        Game game = repository.save(newGame);
        return game;
    }
}
