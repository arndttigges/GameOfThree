package com.takeaway.game.rule;

import com.takeaway.game.dto.GameMove;
import com.takeaway.game.repository.model.Action;
import com.takeaway.game.repository.model.Game;
import com.takeaway.game.repository.model.Mode;
import com.takeaway.game.repository.model.Movement;
import com.takeaway.game.service.GameFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

    private static final String PLAYER_A = "a";
    private static final String PLAYER_B = "b";

    RuleEngine ruleEngine;

    @Mock
    Rule rule;

    @BeforeEach
    void init() {
        ruleEngine = new RuleEngine(rule);
    }

    @Test
    void createMovement() {
        Game game = GameFactory.createNewGame(Mode.REMOTE,PLAYER_B, PLAYER_A, PLAYER_B, 42);
        game.getMovements().get(0).setAction(Action.ZERO);

        GameMove move = new GameMove();
        move.setPlayerId(PLAYER_A);
        move.setAction(Action.ZERO);

        when(rule.isAllowedMove(game, move)).thenReturn(true);
        when(rule.calcNewValue(42, Action.ZERO)).thenReturn(14);
        Optional<Movement> movement = ruleEngine.executeMove(game, move);
        assertAll(
                () -> assertEquals(Action.ZERO, movement.get().getAction()),
                () -> assertEquals(PLAYER_A, movement.get().getPlayerId()),
                () -> assertEquals(2, movement.get().getMovementSequenzNumber()),
                () -> assertEquals(14, movement.get().getNumber())
        );
    }

    @Test
    void noValueIfMovementIsInvalid() {
        Game game = GameFactory.createNewGame(Mode.REMOTE,PLAYER_B, PLAYER_A, PLAYER_B, 42);
        GameMove move = new GameMove();
        move.setPlayerId(PLAYER_A);
        move.setAction(Action.ZERO);

        when(rule.isAllowedMove(game, move)).thenReturn(false);
        assertEquals(Optional.empty(), ruleEngine.executeMove(game, move));
    }

    @Test
    void useOpponentIfIdMatching() {
        String result = ruleEngine.getPlayerFromID(PLAYER_A, "a");
        assertEquals(PLAYER_A, result);
    }

    @Test
    void newPlayerIsUsedIfIdDiffers() {
        String result = ruleEngine.getPlayerFromID(PLAYER_A, "b");
        assertEquals(PLAYER_B, result);
    }

}