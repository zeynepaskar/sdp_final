package com.thanos.participant2.factory;

import com.thanos.participant2.enemies.ChitauriEnemy;
import com.thanos.participant2.enemies.Enemy;

public class ChitauriEnemyFactory extends EnemyFactory {

    @Override
    protected Enemy createEnemy(float x, float y) {
        return new ChitauriEnemy(x, y);
    }
}
