package com.thanos.participant2.factory;

import com.thanos.participant2.enemies.BossEnemy;
import com.thanos.participant2.enemies.BossType;
import com.thanos.participant2.enemies.Enemy;

public class BossEnemyFactory {

    public Enemy create(BossType bossType, float x, float y) {
        return new BossEnemy(bossType, x, y);
    }
}