package com.thanos.participant2.factory;

import com.thanos.participant2.enemies.Enemy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnemyFactoryTest {

    @Test
    void shouldCreateBasicEnemy() {
        EnemyFactory factory = new BasicEnemyFactory();

        Enemy enemy = factory.create(100, 200);

        assertEquals("Outrider", enemy.name());
        assertEquals(100, enemy.health());
        assertEquals(5, enemy.defense());
        assertEquals(100, enemy.getX());
        assertEquals(200, enemy.getY());
    }

    @Test
    void shouldCreateChitauri() {
        EnemyFactory factory = new ChitauriEnemyFactory();

        Enemy enemy = factory.create(50, 60);

        assertEquals("Chitauri", enemy.name());
        assertEquals(80, enemy.health());
        assertEquals(10, enemy.defense());
    }

    @Test
    void shouldCreateSakaaran() {
        EnemyFactory factory = new SakaaranEnemyFactory();

        Enemy enemy = factory.create(10, 20);

        assertEquals("Sakaaran", enemy.name());
        assertEquals(150, enemy.health());
        assertEquals(25, enemy.defense());
    }
}