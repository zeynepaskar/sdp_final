
package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.Enemy;

public class MeleeAttackStrategy implements CombatStrategy {

    private static final float ATTACK_RANGE = 1.5f;

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
