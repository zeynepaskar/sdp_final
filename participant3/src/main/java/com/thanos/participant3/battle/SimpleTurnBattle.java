package com.thanos.participant3.battle;

import com.thanos.participant3.Combatant;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEventType;
import java.util.OptionalInt;

/** Stand-in TurnBattle with a fixed enemy power; every blow goes through HitResolver. */
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
        this(events, new HitResolver(events), thanos, enemy, enemyPower, boss);
    }

    public SimpleTurnBattle(EventBus events, HitResolver resolver, Combatant thanos, Combatant enemy,
                            int enemyPower, boolean boss) {
        if (events == null || resolver == null || thanos == null || enemy == null || enemyPower < 0) {
            throw new IllegalArgumentException("Battle needs events, a resolver, two fighters and nonnegative enemy power");
        }
        this.events = events;
        this.resolver = resolver;
        this.thanos = thanos;
        this.enemy = enemy;
        this.enemyPower = enemyPower;
        this.boss = boss;
        events.publish(GameEventType.BATTLE_STARTED, enemy.name(), enemy.name() + " blocks the way!", 0);
    }

    public Combatant thanos() { return thanos; }
    public Combatant enemy() { return enemy; }
    public BattleState state() { return state; }
    public boolean canFlee() { return !boss; }

    public OptionalInt nextEnemyDamage() {
        if (state.isOver()) return OptionalInt.empty();
        if (stunTurns > 0) return OptionalInt.of(0);
        return OptionalInt.of(HitResolver.damageAgainst(enemyStrike(), thanos));
    }

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
            resolver.resolve(enemy, thanos, enemyStrike());
        }
        slowPercent = 0;
        defending = false;
        if (!enemy.isAlive()) {
            win();
        } else if (thanos.isAlive()) {
            state = BattleState.PLAYER_TURN;
        } else {
            state = BattleState.LOST;
            events.publish(GameEventType.BATTLE_LOST, thanos.name(), thanos.name() + " has fallen...", 0);
        }
    }

    private Attack enemyStrike() {
        int damage = enemyPower * (100 - slowPercent) / 100;
        return new BasicAttack(enemy.name() + " strike", defending ? damage / 2 : damage);
    }

    private void endPlayerTurn() {
        if (enemy.isAlive()) state = BattleState.ENEMY_TURN;
        else win();
    }

    private void win() {
        state = BattleState.WON;
        events.publish(GameEventType.BATTLE_WON, enemy.name(), enemy.name() + " is defeated. Thanos wins!", 0);
    }

    private void expect(BattleState expected) {
        if (state != expected) {
            throw new IllegalStateException("Expected " + expected + " but the battle is in " + state);
        }
    }
}
