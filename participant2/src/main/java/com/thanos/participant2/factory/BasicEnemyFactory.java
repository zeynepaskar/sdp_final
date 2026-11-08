package com.thanos.participant2.factory;

import com.thanos.participant2.enemies.BasicEnemy;
import com.thanos.participant2.enemies.Enemy;

public class BasicEnemyFactory implements EnemyFactory {

    @Override
    public Enemy create(float x, float y) {
        return new BasicEnemy(x, y);
    }
}