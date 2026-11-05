
package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.Enemy;

public interface CombatStrategy {

    float attack(Enemy enemy, float targetX, float targetY);
}
