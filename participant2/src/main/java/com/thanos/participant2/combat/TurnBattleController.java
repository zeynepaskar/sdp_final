package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.strategy.CombatStrategy;
import com.thanos.participant3.Combatant;
import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.battle.TurnBattle;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.DamageScaleDecorator;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEventType;

import java.util.OptionalInt;

public class TurnBattleController implements TurnBattle {

    private final EventBus events;
    private final HitResolver resolver;
    private final Combatant thanos;
    private final Enemy enemy;
    private final CombatStrategy enemyStrategy;
    private final boolean boss;

    private BattleState state = BattleState.PLAYER_TURN;

    // Effects applied to the enemy by Thanos
    private int stunTurns;
    private int slowPercent;

    // Effects applied to Thanos by the enemy
    private int playerStunTurns;
    private boolean recoveringFromStun;
    private int playerDamageReductionPercent;

    private boolean defending;

    // Cache the strategy result so UI previews do not advance boss attack cycles.
    private Attack preparedEnemyBaseAttack;

    public TurnBattleController(
            EventBus events,
            HitResolver resolver,
            Combatant thanos,
            Enemy enemy,
            CombatStrategy enemyStrategy,
            boolean boss
    ) {
        if (events == null || resolver == null || thanos == null
                || enemy == null || enemyStrategy == null) {
            throw new IllegalArgumentException(
                    "Battle requires events, resolver, fighters and strategy"
            );
        }

        this.events = events;
        this.resolver = resolver;
        this.thanos = thanos;
        this.enemy = enemy;
        this.enemyStrategy = enemyStrategy;
        this.boss = boss;

        events.publish(
                GameEventType.BATTLE_STARTED,
                enemy.name(),
                enemy.name() + " blocks the way!",
                0
        );
    }

    @Override
    public Combatant thanos() {
        return thanos;
    }

    @Override
    public Combatant enemy() {
        return enemy;
    }

    @Override
    public BattleState state() {
        return state;
    }

    @Override
    public boolean canFlee() {
        return !boss;
    }

    @Override
    public OptionalInt nextEnemyDamage() {
        if (state.isOver()) {
            return OptionalInt.empty();
        }

        if (stunTurns > 0) {
            return OptionalInt.of(0);
        }
        return OptionalInt.of(HitResolver.damageAgainst(nextEnemyAttack(), thanos));
    }

    @Override
    public void playerAttack(Attack attack) {
        expect(BattleState.PLAYER_TURN);

        if (attack == null) {
            throw new IllegalArgumentException("Attack must not be null");
        }

        // A stunned Thanos loses this turn.
        if (skipPlayerTurnIfStunned()) {
            return;
        }

        // Apply weakening from the enemy's previous attack.
        if (playerDamageReductionPercent > 0) {
            attack = new DamageScaleDecorator(attack, 100 - playerDamageReductionPercent);
            playerDamageReductionPercent = 0;
        }

        resolver.resolve(thanos, enemy, attack);

        // Apply effects to the enemy.
        stunTurns += attack.stunTurns();
        slowPercent = attack.slowPercent();

        endPlayerTurn();
    }

    @Override
    public void playerDefend() {
        expect(BattleState.PLAYER_TURN);

        if (skipPlayerTurnIfStunned()) {
            return;
        }

        defending = true;

        events.publish(
                GameEventType.DEFENDED,
                thanos.name(),
                thanos.name() + " braces for the next blow",
                0
        );

        endPlayerTurn();
    }

    @Override
    public void flee() {
        expect(BattleState.PLAYER_TURN);

        if (!canFlee()) {
            throw new IllegalStateException(
                    "There is no escape from " + enemy.name()
            );
        }

        state = BattleState.FLED;

        events.publish(
                GameEventType.FLED,
                thanos.name(),
                thanos.name() + " left the battle",
                0
        );
    }

    @Override
    public void enemyTurn() {
        expect(BattleState.ENEMY_TURN);

        if (stunTurns > 0) {
            stunTurns--;

            events.publish(
                    GameEventType.TURN_SKIPPED,
                    enemy.name(),
                    enemy.name() + " is stunned and cannot move",
                    0
            );
        } else {
            Attack attack = nextEnemyAttack();
            int dealt = resolver.resolve(enemy, thanos, attack);
            preparedEnemyBaseAttack = null;
            if (dealt > 0 && thanos.isAlive()) {
                if (!recoveringFromStun) playerStunTurns += attack.stunTurns();
                playerDamageReductionPercent = Math.max(playerDamageReductionPercent, attack.slowPercent());
            }
        }

        recoveringFromStun = false;
        slowPercent = 0;
        defending = false;

        if (!enemy.isAlive()) {
            win();
        } else if (thanos.isAlive()) {
            state = BattleState.PLAYER_TURN;
        } else {
            state = BattleState.LOST;

            events.publish(
                    GameEventType.BATTLE_LOST,
                    thanos.name(),
                    thanos.name() + " has fallen...",
                    0
            );
        }
    }

    private Attack nextEnemyAttack() {
        if (preparedEnemyBaseAttack == null) {
            preparedEnemyBaseAttack = enemyStrategy.createAttack(enemy);
        }

        Attack attack = preparedEnemyBaseAttack;
        if (slowPercent > 0) attack = new DamageScaleDecorator(attack, 100 - slowPercent);
        if (defending) attack = new DamageScaleDecorator(attack, 50);
        return attack;
    }

    public boolean skipPlayerTurnIfStunned() {
        if (state != BattleState.PLAYER_TURN || playerStunTurns <= 0) {
            return false;
        }

        playerStunTurns--;
        recoveringFromStun = true;

        events.publish(
                GameEventType.TURN_SKIPPED,
                thanos.name(),
                thanos.name() + " is stunned and skips the turn",
                0
        );

        state = BattleState.ENEMY_TURN;
        return true;
    }

    private void endPlayerTurn() {
        if (enemy.isAlive()) {
            state = BattleState.ENEMY_TURN;
        } else {
            win();
        }
    }

    private void win() {
        state = BattleState.WON;

        events.publish(
                GameEventType.BATTLE_WON,
                enemy.name(),
                enemy.name() + " is defeated. Thanos wins!",
                0
        );
    }

    private void expect(BattleState expected) {
        if (state != expected) {
            throw new IllegalStateException(
                    "Expected " + expected + " but the battle is in " + state
            );
        }
    }
}
