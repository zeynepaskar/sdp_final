package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;

public class MeleeAttackStrategy implements CombatStrategy {

    @Override
    public Attack createAttack(Enemy enemy) {
        if (enemy == null) {
            throw new IllegalArgumentException("Enemy is required");
        }

        return new BasicAttack(
                "Melee Strike",
                enemy.getDamage()
        );
    }
}