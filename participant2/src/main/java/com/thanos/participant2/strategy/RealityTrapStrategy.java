package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.MindStoneDecorator;

/**
 * Collector's trap is telegraphed by a predictable cycle:
 * two ordinary attacks followed by one stunning trap.
 */
public class RealityTrapStrategy implements CombatStrategy {

    public static final int TRAP_INTERVAL = 3;

    private int attackCount;

    @Override
    public Attack createAttack(Enemy enemy) {
        if (enemy == null) {
            throw new IllegalArgumentException("Enemy is required");
        }

        attackCount++;

        BasicAttack basicAttack = new BasicAttack(
                attackCount % TRAP_INTERVAL == 0
                        ? "Reality Trap"
                        : "Collector Strike",
                enemy.getDamage()
        );

        if (attackCount % TRAP_INTERVAL == 0) {
            return new MindStoneDecorator(basicAttack);
        }

        return basicAttack;
    }

    /** Number of attacks already selected in the current battle. */
    public int attackCount() {
        return attackCount;
    }
}
