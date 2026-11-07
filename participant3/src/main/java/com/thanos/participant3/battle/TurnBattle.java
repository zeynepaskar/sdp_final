package com.thanos.participant3.battle;

import com.thanos.participant3.Combatant;
import com.thanos.participant3.patterns.decorator.Attack;
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

    default OptionalInt nextEnemyDamage() { return OptionalInt.empty(); }
}
