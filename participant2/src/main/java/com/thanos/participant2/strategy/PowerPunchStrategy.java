package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.DamageScaleDecorator;

public class PowerPunchStrategy implements CombatStrategy {

    @Override
    public Attack createAttack(Enemy enemy) {
        if (enemy == null) {
            throw new IllegalArgumentException("Enemy is required");
        }

        return new DamageScaleDecorator(
                new BasicAttack("Power Punch", enemy.getDamage()),
                200
        );
    }
}