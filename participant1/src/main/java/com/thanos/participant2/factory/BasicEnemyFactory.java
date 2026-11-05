
package com.thanos.participant2.factory;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.enemies.BasicEnemy;

public class BasicEnemyFactory extends EnemyFactory {

    @Override
    protected Enemy createEnemy(float x, float y) {
        return new BasicEnemy(x, y);
    }
}
