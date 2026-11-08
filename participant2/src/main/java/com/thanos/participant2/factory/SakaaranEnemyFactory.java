package com.thanos.participant2.factory;

import com.thanos.participant2.enemies.SakaaranEnemy;
import com.thanos.participant2.enemies.Enemy;

public class SakaaranEnemyFactory implements EnemyFactory {

    @Override
    public Enemy create(float x, float y) {
        return new SakaaranEnemy(x, y);
    }
}