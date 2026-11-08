package com.thanos.participant2.factory;

import com.thanos.participant2.enemies.Enemy;

public interface EnemyFactory {

    Enemy create(float x, float y);
}