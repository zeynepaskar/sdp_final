package com.thanos.participant3.battle;

import com.thanos.participant3.Combatant;
import com.thanos.participant3.patterns.decorator.Attack;
import java.util.Optional;
import java.util.OptionalInt;

/** Contract between BattleScreen and participant 2's battle rules; results are published on the EventBus. */
public interface TurnBattle {
    Combatant thanos();
    Combatant enemy();
    BattleState state();
    boolean canFlee();
    void playerAttack(Attack attack);
    void playerDefend();
    void flee();
    void enemyTurn();

    default boolean skipPlayerTurnIfStunned() { return false; }

    default OptionalInt nextEnemyDamage() { return OptionalInt.empty(); }

    /** The attack the enemy is about to make, or empty when it will skip its turn or the battle is over. */
    default Optional<Attack> upcomingEnemyAttack() { return Optional.empty(); }
}
