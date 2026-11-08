package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant3.patterns.decorator.Attack;

public interface CombatStrategy {

    Attack createAttack(Enemy enemy);
}