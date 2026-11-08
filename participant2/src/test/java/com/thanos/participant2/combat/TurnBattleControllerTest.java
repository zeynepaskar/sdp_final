package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.BasicEnemy;
import com.thanos.participant2.strategy.MeleeAttackStrategy;
import com.thanos.participant3.SimpleCombatant;
import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.EventBus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TurnBattleControllerTest {

    private TurnBattleController createBattle(boolean boss) {
        EventBus events = new EventBus();

        return new TurnBattleController(
                events,
                new HitResolver(events),
                new SimpleCombatant("Thanos", 100, 20),
                new BasicEnemy(0, 0),
                new MeleeAttackStrategy(),
                boss
        );
    }

    @Test
    void playerAttackDamagesEnemy() {
        TurnBattleController battle = createBattle(false);

        battle.playerAttack(new BasicAttack("Power Strike", 100));

        assertEquals(5, battle.enemy().health());
        assertEquals(BattleState.ENEMY_TURN, battle.state());
    }

    @Test
    void enemyAttackDamagesThanos() {
        TurnBattleController battle = createBattle(false);

        battle.playerAttack(new BasicAttack("Weak Strike", 1));
        battle.enemyTurn();

        assertEquals(88, battle.thanos().health());
        assertEquals(BattleState.PLAYER_TURN, battle.state());
    }

    @Test
    void defenseReducesIncomingDamage() {
        TurnBattleController battle = createBattle(false);

        battle.playerDefend();
        battle.enemyTurn();

        assertEquals(95, battle.thanos().health());
        assertEquals(BattleState.PLAYER_TURN, battle.state());
    }

    @Test
    void stunSkipsEnemyTurn() {
        TurnBattleController battle = createBattle(false);

        Attack stunningAttack = new Attack() {
            @Override
            public int damage() {
                return 1;
            }

            @Override
            public String description() {
                return "Stunning Strike";
            }

            @Override
            public int stunTurns() {
                return 1;
            }
        };

        battle.playerAttack(stunningAttack);
        battle.enemyTurn();

        assertEquals(100, battle.thanos().health());
        assertEquals(BattleState.PLAYER_TURN, battle.state());
    }

    @Test
    void defeatingEnemyWinsBattle() {
        TurnBattleController battle = createBattle(false);

        battle.playerAttack(new BasicAttack("Ultimate Strike", 200));

        assertEquals(0, battle.enemy().health());
        assertEquals(BattleState.WON, battle.state());
    }

    @Test
    void playerCannotFleeFromBoss() {
        TurnBattleController battle = createBattle(true);

        assertFalse(battle.canFlee());

        assertThrows(
                IllegalStateException.class,
                battle::flee
        );

        assertEquals(BattleState.PLAYER_TURN, battle.state());
    }
}