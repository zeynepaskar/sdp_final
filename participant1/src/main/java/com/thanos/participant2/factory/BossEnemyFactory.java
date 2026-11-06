
package com.thanos.participant2.factory;

import com.thanos.participant2.enemies.BossEnemy;
import com.thanos.participant2.enemies.BossType;
import com.thanos.participant2.enemies.Enemy;

public class BossEnemyFactory extends EnemyFactory {

    private final BossType bossType;

    public BossEnemyFactory(BossType bossType) {
        this.bossType = bossType;
    }

    @Override
    protected Enemy createEnemy(float x, float y) {
        return new BossEnemy(x, y, bossType);
    }
}

