package com.takeaway.game.rule;

import com.takeaway.game.dto.GameMove;
import com.takeaway.game.model.Action;
import com.takeaway.game.model.Game;
import com.takeaway.game.model.Mode;
import com.takeaway.game.service.GameFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DivideByThreeRuleTest {

    private static final String PLAYER_A = "a";
    private static final String PLAYER_B = "b";
    DivideByThreeRule rule;

    @BeforeEach
    void init() {
        rule = new DivideByThreeRule();
    }

    @CsvSource(value = {
            "-1, false", "0, false", "1, false", "2, true"
    })
    @ParameterizedTest
    void ruleIsCheckingNumberConditionCorrectly(int value, boolean result) {
        Game game = GameFactory.createNewGame(Mode.REMOTE, PLAYER_B, PLAYER_A, PLAYER_B, value);
        GameMove move = new GameMove();
        move.setPlayerId(PLAYER_A);
        move.setAction(Action.ZERO);

        assertEquals(result, rule.isAllowedMove(game, move));
    }

    @Test
    void playerCanNotMakeAnotherMove() {
        Game game = GameFactory.createNewGame(Mode.REMOTE, PLAYER_B, PLAYER_A, PLAYER_A, 42);
        GameMove move = new GameMove();
        move.setPlayerId(PLAYER_A);
        move.setAction(Action.ZERO);

        assertFalse(rule.isAllowedMove(game, move));
    }

    @CsvSource(value = {
            "-1, ZERO , 0", "0, ZERO , 0", "1, ZERO , 0", "3, ZERO , 1", "12, ZERO , 4",
            "0, MINUS , 0", "0, MINUS , 0", "1, MINUS , 0", "4, MINUS , 1", "13, MINUS , 4",
            "-1, PLUS , 0", "0, ZERO , 0", "2, ZERO , 0", "5, ZERO , 1", "12, ZERO , 4"
    })
    @ParameterizedTest
    void calculateCorrectValues(int value, Action action, int result) {
        int number = rule.calcNewValue(value, action);
        assertEquals(result, number);
    }
}