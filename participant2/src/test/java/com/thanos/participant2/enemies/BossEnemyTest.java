package com.thanos.participant2.enemies;

import com.thanos.participant2.factory.BossEnemyFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BossEnemyTest {

    @Test
    void shouldCreateAllSixBosses() {
        BossEnemyFactory factory = new BossEnemyFactory();

        for (BossType type : BossType.values()) {
            Enemy enemy = factory.create(type, 100, 200);

            assertEquals(type.getDisplayName(), enemy.name());
            assertEquals(type.getHealth(), enemy.health());
            assertEquals(type.getDamage(), enemy.getDamage());
            assertEquals(type.getDefense(), enemy.defense());
            assertTrue(enemy.isAlive());
        }

        assertEquals(6, BossType.values().length);
    }

    @Test
    void bossShouldHaveUniqueAbility() {
        BossEnemy boss = new BossEnemy(
                BossType.CHAMPION, 100, 200
        );

        assertEquals("Power Punch", boss.getAbilityName());
    }

    @Test
    void bossShouldUsePercentageDefense() {
        BossEnemy boss = new BossEnemy(
                BossType.COLLECTOR, 100, 200
        );

        assertEquals(35, boss.defense());
    }
}