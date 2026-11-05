
package com.thanos.participant2.factory;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.enemies.BossEnemy;

public class BossEnemyFactory extends EnemyFactory {

    @Override
    protected Enemy createEnemy(float x, float y) {
        return new BossEnemy(x, y);
    }
}
