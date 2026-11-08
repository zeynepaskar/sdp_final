
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
    void collectorStunsOnlyEveryThirdAttack() {
        TurnBattleController battle = createBattle(
                BossType.COLLECTOR,
                new RealityTrapStrategy()
        );
        BasicAttack attack = new BasicAttack("Punch", 50);

        // Attack 1: ordinary strike, Thanos can act.
        battle.playerDefend();
        battle.enemyTurn();
        assertEquals(BattleState.PLAYER_TURN, battle.state());
        assertFalse(battle.skipPlayerTurnIfStunned());

        // Attack 2: ordinary strike, Thanos can act again.
        battle.playerAttack(attack);
        battle.enemyTurn();
        assertEquals(BattleState.PLAYER_TURN, battle.state());
        assertFalse(battle.skipPlayerTurnIfStunned());

        // Attack 3: Reality Trap stuns Thanos for exactly one turn.
        battle.playerAttack(attack);
        battle.enemyTurn();
        assertTrue(battle.skipPlayerTurnIfStunned());
        assertEquals(BattleState.ENEMY_TURN, battle.state());

        // Attack 4 is ordinary again; after the skipped turn Thanos can attack.
        battle.enemyTurn();
        int healthBefore = battle.enemy().health();
        battle.playerAttack(attack);
        assertTrue(battle.enemy().health() < healthBefore);
    }

    @Test
    void repeatedDamagePreviewsDoNotAdvanceCollectorAttackCycle() {
        TurnBattleController battle = createBattle(
                BossType.COLLECTOR,
                new RealityTrapStrategy()
        );

        assertTrue(battle.nextEnemyDamage().isPresent());
        assertTrue(battle.nextEnemyDamage().isPresent());
        assertTrue(battle.nextEnemyDamage().isPresent());

        battle.playerDefend();
        battle.enemyTurn();

        // The first actual attack was ordinary; previews did not consume turns.
        assertEquals(BattleState.PLAYER_TURN, battle.state());
        assertFalse(battle.skipPlayerTurnIfStunned());
    }

    @Test
    void aTrapThatWentThroughTheSpacePortalCannotStun() {
        EventBus events = new EventBus();
        SimpleCombatant thanos = new SimpleCombatant("Thanos", 500, 20);
        CombatManager manager = new CombatManager(events, thanos, target -> target == thanos);
        TurnBattleController battle = manager.startBossBattle(BossType.COLLECTOR, 0, 0);

        battle.playerDefend();
        battle.enemyTurn();

        assertEquals(500, thanos.health());
        assertFalse(battle.skipPlayerTurnIfStunned());
        battle.playerAttack(new BasicAttack("Punch", 50));
        assertTrue(battle.enemy().health() < battle.enemy().maxHealth());
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
