
package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.Enemy;

public class RangedAttackStrategy implements CombatStrategy {

    private static final float ATTACK_RANGE = 6.0f;

    @Override
    public float attack(Enemy enemy, float targetX, float targetY) {

        double distance = Math.hypot(
                enemy.getX() - targetX,
                enemy.getY() - targetY
        );

        if (distance <= ATTACK_RANGE) {
            return enemy.getDamage();
        }

        return 0;
    }
}