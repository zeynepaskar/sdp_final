package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.battle.SimpleTurnBattle;
import com.thanos.participant3.patterns.decorator.*;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.stones.AbilityBook;
import com.thanos.participant3.ui.EnemyIntent;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class EnemyIntentTest {
    @Test
    void foresightNamesTheAttackAndItsEffects() {
        Attack trap = new MindStoneDecorator(new BasicAttack("Reality Trap", 40));
        Attack strike = new SpaceStoneDecorator(new BasicAttack("Teleport Strike", 50));
        Attack control = new WeakenDecorator(new BasicAttack("Mind Control", 45), 40);

        assertEquals("Reality Trap:  36 damage, stuns you", EnemyIntent.describe(trap, 36));
        assertEquals("Teleport Strike:  50 damage, ignores defense", EnemyIntent.describe(strike, 50));
        assertEquals("Mind Control:  40 damage, your next hit -40%", EnemyIntent.describe(control, 40));
    }

    @Test
    void withoutTheAttackOnlyTheDamageIsKnown() {
        assertEquals("next blow 13", EnemyIntent.describe(null, 13));
        assertEquals("the enemy is stunned and will skip its turn", EnemyIntent.describe(null, 0));
    }

    @Test
    void decoratorsKeepTheNameOfTheBlow() {
        assertEquals("Punch", new PowerStoneDecorator(new SoulStoneDecorator(new BasicAttack("Punch", 50))).name());
    }

    @Test
    void theBattleTellsItsNextAttackAndItMatchesTheBlow() {
        FakeCombatant thanos = new FakeCombatant("Thanos", 1000, 1000, 10);
        SimpleTurnBattle battle = new SimpleTurnBattle(new EventBus(), thanos,
                new FakeCombatant("Champion", 800, 800, 10), 90, true);

        assertEquals("Champion strike", battle.nextEnemyAttack().orElseThrow().name());
        assertEquals(OptionalInt.of(81), battle.nextEnemyDamage());

        battle.playerAttack(new MindStoneDecorator(new BasicAttack("Punch", 10)));
        assertTrue(battle.nextEnemyAttack().isEmpty());
        assertEquals(OptionalInt.of(0), battle.nextEnemyDamage());
        battle.enemyTurn();

        battle.playerDefend();
        battle.enemyTurn();
        assertEquals(1000 - 40, thanos.health());
    }

    @Test
    void onlyUpgradedFormsAreMarked() {
        assertFalse(AbilityBook.isUpgraded(AbilityBook.baseSkills().get(0).basic()));
        assertTrue(AbilityBook.isUpgraded(AbilityBook.baseSkills().get(0).upgraded()));
        assertEquals(List.of(), AbilityBook.abilities(List.of()).stream().filter(AbilityBook::isUpgraded).toList());
    }
}
