package com.takeaway.game.rule;

import com.takeaway.game.dto.GameMove;
import com.takeaway.game.model.*;
import com.takeaway.game.model.GameFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

    private static final Player PLAYER_A = Player.builder().id("a").build();
    private static final Player PLAYER_B = Player.builder().id("b").build();

    RuleEngine ruleEngine;

    @Mock
    Rule rule;

    @BeforeEach
    void init() {
        ruleEngine = new RuleEngine(rule);
    }

    @Test
    void createMovement() {
        Game game = GameFactory.createNewGame(GameMode.REMOTE, PLAYER_A, PLAYER_B, 42);
        game.getMovements().get(0).setAction(Action.ZERO);
        GameMove move = new GameMove(Action.ZERO, PLAYER_A.getId());

        when(rule.isAllowedMove(game,move)).thenReturn(true);
        when(rule.calcNewValue(42, Action.ZERO)).thenReturn(14);
        Optional<Movement> movement = ruleEngine.executeMove(game, move);
        assertAll(
                () -> assertEquals(Action.ZERO, movement.get().getAction()),
                () -> assertEquals(PLAYER_A, movement.get().getPlayer()),
                () -> assertEquals(2, movement.get().getMovementSequenzNumber()),
                () -> assertEquals(14, movement.get().getNumber())
        );
    }

    @Test
    void noValueIfMovementIsInvalid() {
        Game game = GameFactory.createNewGame(GameMode.REMOTE, PLAYER_A, PLAYER_B, 42);
        GameMove move = new GameMove(Action.ZERO, PLAYER_A.getId());

        when(rule.isAllowedMove(game,move)).thenReturn(false);
        assertEquals(Optional.empty(), ruleEngine.executeMove(game, move));
    }

    @Test
    void useOpponentIfIdMatching() {
        Player result = ruleEngine.getPlayerFromID(PLAYER_A, "a");
        assertEquals(PLAYER_A, result);
    }

    @Test
    void newPlayerIsUsedIfIdDiffers() {
        Player result = ruleEngine.getPlayerFromID(PLAYER_A, "b");
        assertEquals(PLAYER_B, result);
    }

}