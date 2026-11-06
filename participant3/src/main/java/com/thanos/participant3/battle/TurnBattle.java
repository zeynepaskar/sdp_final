package com.thanos.participant3.battle;

import com.thanos.participant3.Combatant;
import com.thanos.participant3.patterns.decorator.Attack;

/**
 * Contract between the battle screen (participant 3) and the battle rules (participant 2).
 * Implementations publish what happens on the shared EventBus so the screen can show it.
 */
public interface TurnBattle {
    Combatant thanos();
    Combatant enemy();
    BattleState state();
    /** Bosses cannot be escaped. */
    boolean canFlee();
    /** Thanos's move; the attack may already be wrapped in stone decorators. */
    void playerAttack(Attack attack);
    /** Thanos takes half damage from the next enemy move. */
    void playerDefend();
    void flee();
    /** The enemy picks and performs its move. */
    void enemyTurn();
}
