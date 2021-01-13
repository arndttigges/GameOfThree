package com.takeaway.game.service;

import com.takeaway.game.dto.GameTemplate;
import com.takeaway.game.dto.GameView;
import com.takeaway.game.model.Movement;
import com.takeaway.game.repository.GameRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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
                    return GameView.builder()
                            .opponentAddress(game.getOpponent().getAddress())
                            .lastStep(lastMove.getAction())
                            .sequenceNumber(lastMove.getMovementSequenzNumber())
                            .number(lastMove.getNumber())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public void createNewGame(GameTemplate gameTemplate) {


    }
}
