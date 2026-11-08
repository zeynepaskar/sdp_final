
package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.BossEnemy;
import com.thanos.participant2.enemies.BossType;
import com.thanos.participant2.strategy.CombatStrategy;
import com.thanos.participant2.strategy.RealityTrapStrategy;
import com.thanos.participant2.strategy.MindControlStrategy;
import com.thanos.participant2.strategy.TeleportStrikeStrategy;
import com.thanos.participant2.strategy.PowerPunchStrategy;

import com.thanos.participant3.SimpleCombatant;
import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.EventBus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BossAbilityTest {

    private TurnBattleController createBattle(
            BossType bossType,
            CombatStrategy strategy
    ) {
        EventBus events = new EventBus();

        return new TurnBattleController(
                events,
                new HitResolver(events),
                new SimpleCombatant("Thanos", 500, 20),
                new BossEnemy(bossType, 0, 0),
                strategy,
                true
        );
    }

    @Test
    void collectorStunsThanos() {
        TurnBattleController battle = createBattle(
                BossType.COLLECTOR,
                new RealityTrapStrategy()
        );

        battle.playerDefend();
        battle.enemyTurn();

        int healthBefore = battle.thanos().health();

        battle.playerAttack(new BasicAttack("Test Attack", 50));

        assertEquals(healthBefore, battle.thanos().health());
        assertEquals(BattleState.ENEMY_TURN, battle.state());
    }

    @Test
    void grandmasterWeakensThanosNextAttack() {
        TurnBattleController battle = createBattle(
                BossType.GRANDMASTER,
                new MindControlStrategy()
        );

        battle.playerDefend();
        battle.enemyTurn();

        int healthBefore = battle.enemy().health();

        battle.playerAttack(new BasicAttack("Test Attack", 100));

        int actualDamage = healthBefore - battle.enemy().health();

        assertEquals(42, actualDamage);
    }

    @Test
    void runnerIgnoresThanosDefense() {
        TurnBattleController battle = createBattle(
                BossType.RUNNER,
                new TeleportStrikeStrategy()
        );

        battle.playerDefend();
        battle.enemyTurn();

        assertEquals(475, battle.thanos().health());
    }

    @Test
    void championDealsDoubleDamage() {
        TurnBattleController battle = createBattle(
                BossType.CHAMPION,
                new PowerPunchStrategy()
        );

        battle.playerDefend();
        battle.enemyTurn();

        assertEquals(452, battle.thanos().health());
    }
}
