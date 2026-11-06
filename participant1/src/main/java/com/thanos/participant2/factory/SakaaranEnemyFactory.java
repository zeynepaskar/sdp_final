package com.thanos.participant2.factory;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.enemies.SakaaranEnemy;

public class SakaaranEnemyFactory extends EnemyFactory {

    @Override
    protected Enemy createEnemy(float x, float y) {
        return new SakaaranEnemy(x, y);
    }
}
