
package com.thanos.participant2.factory;

import com.thanos.participant2.enemies.Enemy;

public abstract class EnemyFactory {

    // Factory Method
    protected abstract Enemy createEnemy(float x, float y);

    // Common method for creating enemies
    public Enemy spawnEnemy(float x, float y) {
        return createEnemy(x, y);
    }
}
