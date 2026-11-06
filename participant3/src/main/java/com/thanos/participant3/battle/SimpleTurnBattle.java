package com.thanos.participant3.battle;

import com.thanos.participant3.Combatant;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEventType;

/**
 * Stand-in battle rules so the screen can be built and shown before participant 2's
 * implementation exists: the enemy always hits with the same power.
 */
public final class SimpleTurnBattle implements TurnBattle {
    private final EventBus events;
    private final HitResolver resolver;
    private final Combatant thanos;
    private final Combatant enemy;
    private final int enemyPower;
    private final boolean boss;
    private BattleState state = BattleState.PLAYER_TURN;
    private boolean defending;
    private int stunTurns;
    private int slowPercent;

    public SimpleTurnBattle(EventBus events, Combatant thanos, Combatant enemy, int enemyPower, boolean boss) {
        if (events == null || thanos == null || enemy == null || enemyPower < 0) {
            throw new IllegalArgumentException("Battle needs events, two fighters and nonnegative enemy power");
        }
        this.events = events;
        this.thanos = thanos;
        this.enemy = enemy;
        this.enemyPower = enemyPower;
        this.boss = boss;
        resolver = new HitResolver(events);
        events.publish(GameEventType.BATTLE_STARTED, enemy.name(), enemy.name() + " blocks the way!", 0);
    }

    public Combatant thanos() { return thanos; }
    public Combatant enemy() { return enemy; }
    public BattleState state() { return state; }
    public boolean canFlee() { return !boss; }

    public void playerAttack(Attack attack) {
        expect(BattleState.PLAYER_TURN);
        if (attack == null) {
            throw new IllegalArgumentException("Attack must not be null");
        }
        resolver.resolve(thanos, enemy, attack);
        stunTurns += attack.stunTurns();
        slowPercent = attack.slowPercent();
        endPlayerTurn();
    }

    public void playerDefend() {
        expect(BattleState.PLAYER_TURN);
        defending = true;
        events.publish(GameEventType.DEFENDED, thanos.name(), thanos.name() + " braces for the next blow", 0);
        endPlayerTurn();
    }

    public void flee() {
        expect(BattleState.PLAYER_TURN);
        if (!canFlee()) {
            throw new IllegalStateException("There is no escape from " + enemy.name());
        }
        state = BattleState.FLED;
        events.publish(GameEventType.FLED, thanos.name(), thanos.name() + " left the battle", 0);
    }

    public void enemyTurn() {
        expect(BattleState.ENEMY_TURN);
        if (stunTurns > 0) {
            stunTurns--;
            events.publish(GameEventType.TURN_SKIPPED, enemy.name(), enemy.name() + " is stunned and cannot move", 0);
        } else {
            int damage = enemyPower * (100 - slowPercent) / 100;
            if (defending) damage /= 2;
            resolver.resolve(enemy, thanos, new BasicAttack(enemy.name() + " strike", damage));
        }
        slowPercent = 0;
        defending = false;
        if (thanos.isAlive()) {
            state = BattleState.PLAYER_TURN;
        } else {
            state = BattleState.LOST;
            events.publish(GameEventType.BATTLE_LOST, thanos.name(), thanos.name() + " has fallen...", 0);
        }
    }

    private void endPlayerTurn() {
        if (enemy.isAlive()) {
            state = BattleState.ENEMY_TURN;
        } else {
            state = BattleState.WON;
            events.publish(GameEventType.BATTLE_WON, enemy.name(), enemy.name() + " is defeated. Thanos wins!", 0);
        }
    }

    private void expect(BattleState expected) {
        if (state != expected) {
            throw new IllegalStateException("Expected " + expected + " but the battle is in " + state);
        }
    }
}
