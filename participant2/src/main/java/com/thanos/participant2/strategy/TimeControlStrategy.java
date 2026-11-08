package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.TimeStoneDecorator;

public class TimeControlStrategy implements CombatStrategy {

    @Override
    public Attack createAttack(Enemy enemy) {
        if (enemy == null) {
            throw new IllegalArgumentException("Enemy is required");
        }

        return new TimeStoneDecorator(
                new BasicAttack("Time Control", enemy.getDamage())
        );
    }
}