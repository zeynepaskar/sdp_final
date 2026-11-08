package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.SoulStoneDecorator;

public class SoulBlastStrategy implements CombatStrategy {

    @Override
    public Attack createAttack(Enemy enemy) {
        if (enemy == null) {
            throw new IllegalArgumentException("Enemy is required");
        }

        return new SoulStoneDecorator(
                new BasicAttack("Soul Blast", enemy.getDamage())
        );
    }
}