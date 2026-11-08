
package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.BasicEnemy;
import com.thanos.participant2.enemies.BossType;
import com.thanos.participant2.strategy.MeleeAttackStrategy;
import com.thanos.participant3.SimpleCombatant;
import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.patterns.observer.EventBus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatManagerTest {

    private CombatManager createManager() {
        return new CombatManager(
                new EventBus(),
                new SimpleCombatant("Thanos", 500, 20)
        );
    }

    @Test
    void shouldStartRegularBattle() {
        CombatManager manager = createManager();

        TurnBattleController battle = manager.startBattle(
                new BasicEnemy(0, 0),
                new MeleeAttackStrategy()
        );

        assertNotNull(battle);
        assertTrue(manager.hasActiveBattle());
        assertEquals(BattleState.PLAYER_TURN, battle.state());
    }

    @Test
    void shouldStartBossBattle() {
        CombatManager manager = createManager();

        TurnBattleController battle = manager.startBossBattle(
                BossType.CHAMPION, 100, 100
        );

        assertNotNull(battle);
        assertTrue(manager.hasActiveBattle());
        assertFalse(battle.canFlee());
    }

    @Test
    void shouldRejectSecondActiveBattle() {
        CombatManager manager = createManager();

        manager.startBattle(
                new BasicEnemy(0, 0),
                new MeleeAttackStrategy()
        );

        assertThrows(
                IllegalStateException.class,
                () -> manager.startBossBattle(
                        BossType.CHAMPION, 0, 0
                )
        );
    }

    @Test
    void shouldFinishCompletedBattle() {
        CombatManager manager = createManager();

        TurnBattleController battle = manager.startBattle(
                new BasicEnemy(0, 0),
                new MeleeAttackStrategy()
        );

        battle.playerAttack(
                new com.thanos.participant3.patterns.decorator.BasicAttack(
                        "Ultimate", 1000
                )
        );

        assertEquals(BattleState.WON, battle.state());

        manager.finishBattle();

        assertFalse(manager.hasActiveBattle());
        assertTrue(manager.currentBattle().isEmpty());
    }
    @Test
    void shouldNotReplaceFinishedBattleBeforeItIsReleased() {
        CombatManager manager = createManager();

        TurnBattleController battle = manager.startBattle(
                new BasicEnemy(0, 0),
                new MeleeAttackStrategy()
        );

        battle.playerAttack(
                new com.thanos.participant3.patterns.decorator.BasicAttack(
                        "Ultimate", 1000
                )
        );

        assertEquals(BattleState.WON, battle.state());

        assertThrows(
                IllegalStateException.class,
                () -> manager.startBattle(
                        new BasicEnemy(0, 0),
                        new MeleeAttackStrategy()
                )
        );

        manager.finishBattle();

        assertDoesNotThrow(
                () -> manager.startBattle(
                        new BasicEnemy(0, 0),
                        new MeleeAttackStrategy()
                )
        );
    }
}
