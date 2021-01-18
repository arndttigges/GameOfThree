package com.takeaway.game.rule;

import com.takeaway.game.dto.GameMove;
import com.takeaway.game.model.Action;
import com.takeaway.game.model.Game;
import com.takeaway.game.model.Movement;
import com.takeaway.game.model.Player;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class RuleEngine {

    Rule rule;

    /**
     * executes a game move for a game
     * @return Movement or empty, if move is invalid
     */
    public Optional<Movement> executeMove(Game game, GameMove gameMove) {
        if( rule.isAllowedMove(game, gameMove)) {
            Movement lastMovement = getLastMovement(game);

            int newValue = rule.calcNewValue(lastMovement.getNumber(), gameMove.getMove());
            int newSequenceNumber = lastMovement.getMovementSequenzNumber() + 1;
            Player player = getPlayerFromID(game.getOpponent(), gameMove.getPlayerId());
            Action newAction = gameMove.getMove();

            return Optional.of(Movement.builder()
                    .action(newAction)
                    .player(player)
                    .number(newValue)
                    .movementSequenzNumber(newSequenceNumber)
                    .build());
        }
        return Optional.empty();
    }

    private Player getPlayerFromID(Player opponent, String playerId) {
        return opponent.getId().equals(playerId)
                ? opponent
                : Player.builder().id(playerId).build();
    }

    private Movement getLastMovement(Game game) {
        int lastElementIndex = game.getMovements().size() - 1;
        return game.getMovements().get(lastElementIndex);
    }

}
