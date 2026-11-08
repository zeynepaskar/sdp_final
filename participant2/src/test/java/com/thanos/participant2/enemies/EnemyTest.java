package com.thanos.participant2.enemies;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnemyTest {

    @Test
    void shouldCreateAllRegularEnemies() {
        Enemy outrider = new BasicEnemy(100, 100);
        Enemy chitauri = new ChitauriEnemy(200, 100);
        Enemy sakaaran = new SakaaranEnemy(300, 100);

        assertEquals("Outrider", outrider.name());
        assertEquals(100, outrider.health());
        assertEquals(5, outrider.defense());

        assertEquals("Chitauri", chitauri.name());
        assertEquals(80, chitauri.health());
        assertEquals(10, chitauri.defense());

        assertEquals("Sakaaran", sakaaran.name());
        assertEquals(150, sakaaran.health());
        assertEquals(25, sakaaran.defense());
    }

    @Test
    void shouldReduceEnemyHealth() {
        Enemy enemy = new BasicEnemy(0, 0);

        enemy.takeDamage(30);

        assertEquals(70, enemy.health());
    }

    @Test
    void shouldNotAllowHealthBelowZero() {
        Enemy enemy = new BasicEnemy(0, 0);

        enemy.takeDamage(500);

        assertEquals(0, enemy.health());
        assertFalse(enemy.isAlive());
    }

    @Test
    void shouldNotHealAboveMaxHealth() {
        Enemy enemy = new BasicEnemy(0, 0);

        enemy.takeDamage(50);
        enemy.heal(100);

        assertEquals(100, enemy.health());
    }
}