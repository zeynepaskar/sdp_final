
package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.Enemy;

public abstract class SpecialBossAttackStrategy implements CombatStrategy {

    private final float attackRange;
    private final float multiplier;

    protected SpecialBossAttackStrategy(
            float attackRange, float multiplier) {
        this.attackRange = attackRange;
        this.multiplier = multiplier;
    }

    @Override
    public float attack(Enemy enemy, float targetX, float targetY) {

        double distance = Math.hypot(
                enemy.getX() - targetX,
                enemy.getY() - targetY
        );

        if (distance <= attackRange) {
            return enemy.getDamage() * multiplier;
        }

        return 0;
    }
}