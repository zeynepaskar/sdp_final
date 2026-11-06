
package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.BasicEnemy;
import com.thanos.participant2.enemies.ChitauriEnemy;
import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.enemies.SakaaranEnemy;

public class RegularEnemyStrategySelector {

    public static CombatStrategy getStrategy(Enemy enemy) {

        if (enemy instanceof BasicEnemy) {
            return new MeleeAttackStrategy();
        }

        if (enemy instanceof ChitauriEnemy) {
            return new RangedAttackStrategy();
        }

        if (enemy instanceof SakaaranEnemy) {
            return new HeavyAttackStrategy();
        }

        throw new IllegalArgumentException(
                "Unknown regular enemy type"
        );
    }
}
