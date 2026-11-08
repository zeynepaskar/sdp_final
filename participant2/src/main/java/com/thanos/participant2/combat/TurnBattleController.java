
package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.strategy.CombatStrategy;
import com.thanos.participant3.Combatant;
import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.battle.TurnBattle;
import com.thanos.participant3.patterns.decorator.Attack;
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
    private int playerDamageReductionPercent;

    private boolean defending;

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

        Attack attack = enemyStrategy.createAttack(enemy);

        int damage = attack.damage() * (100 - slowPercent) / 100;

        if (defending) {
            damage = damage * 50 / 100;
        }

        int defense = Math.max(0, Math.min(80, thanos.defense()));

        if (!attack.ignoresDefense() && !attack.unblockable()) {
            damage = damage * (100 - defense) / 100;
        }

        return OptionalInt.of(damage);
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
            attack = scaleAttack(attack, playerDamageReductionPercent);
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
            Attack attack = enemyStrategy.createAttack(enemy);

            // Apply weakening from Thanos's previous attack.
            if (slowPercent > 0) {
                attack = scaleAttack(attack, slowPercent);
            }

            // Apply Thanos's defense stance.
            if (defending) {
                attack = scaleAttack(attack, 50);
            }

            int incomingStun = attack.stunTurns();
            int incomingWeakening = attack.slowPercent();

            resolver.resolve(enemy, thanos, attack);

            // Apply effects to Thanos if he survived.
            if (thanos.isAlive()) {
                playerStunTurns += incomingStun;

                playerDamageReductionPercent = Math.max(
                        playerDamageReductionPercent,
                        incomingWeakening
                );
            }
        }

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

    private Attack scaleAttack(Attack original, int reductionPercent) {
        int safeReduction = Math.max(0, Math.min(100, reductionPercent));

        int scaledDamage =
                original.damage() * (100 - safeReduction) / 100;

        return new Attack() {
            @Override
            public int damage() {
                return scaledDamage;
            }

            @Override
            public String description() {
                return original.description();
            }

            @Override
            public int lifestealPercent() {
                return original.lifestealPercent();
            }

            @Override
            public int selfHeal() {
                return original.selfHeal();
            }

            @Override
            public int rewindPercent() {
                return original.rewindPercent();
            }

            @Override
            public boolean ignoresDefense() {
                return original.ignoresDefense();
            }

            @Override
            public boolean unblockable() {
                return original.unblockable();
            }

            @Override
            public int slowPercent() {
                return original.slowPercent();
            }

            @Override
            public int stunTurns() {
                return original.stunTurns();
            }
        };
    }

    private boolean skipPlayerTurnIfStunned() {
        if (playerStunTurns <= 0) {
            return false;
        }

        playerStunTurns--;

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
