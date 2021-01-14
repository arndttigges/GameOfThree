package com.takeaway.game.service;

import com.takeaway.game.dto.DetailedGame;
import com.takeaway.game.dto.GameTemplate;
import com.takeaway.game.dto.GameView;
import com.takeaway.game.dto.Move;
import com.takeaway.game.model.Game;
import com.takeaway.game.model.Movement;
import com.takeaway.game.model.Player;
import com.takeaway.game.repository.GameRepository;
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

    public List<GameView> getAllRunningGames() {
        return repository.findAll()
                .stream()
                .map(game -> {
                    Movement lastMove = game.getMovements().get(game.getMovements().size() - 1);
                    System.out.println(game.getId());
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

    public DetailedGame fetchGame(UUID gameID) {
        Optional<Game> gameOptional = repository.findById(gameID);

        if(gameOptional.isPresent()) {
            Game game = gameOptional.get();

            List<Move> moves = game.getMovements()
                    .stream()
                    .map(movement -> Move.builder()
                            .sequenceNumber(movement.getMovementSequenzNumber())
                            .number(movement.getNumber())
                            .myAction(movement.getAction())
                            .build())
                    .collect(Collectors.toList());


            return DetailedGame.builder().movements(moves).build();
        }
        return null;
    }

    public Game createNewGame(GameTemplate gameTemplate) {
        Player opponent = Player.builder()
                .address(gameTemplate.getAddress())
                .build();
        Movement startMove = Movement.builder()
                .movementSequenzNumber(1)
                .number(gameTemplate.getStartValue())
                .build();

        Game newGame = Game.builder()
                .id(UUID.randomUUID())
                .opponent(opponent)
                .movements(List.of(startMove))
                .build();

        return repository.save(newGame);
    }
}
