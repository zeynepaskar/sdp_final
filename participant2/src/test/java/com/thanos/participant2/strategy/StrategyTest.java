package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.ChitauriEnemy;
import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant3.patterns.decorator.Attack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StrategyTest {

    @Test
    void meleeShouldDealBaseDamage() {
        Enemy enemy = new ChitauriEnemy(0, 0);

        Attack attack = new MeleeAttackStrategy().createAttack(enemy);

        assertEquals(12, attack.damage());
        assertEquals("Melee Strike", attack.description().split(" \\(")[0]);
    }

    @Test
    void rangedShouldDealReducedDamage() {
        Enemy enemy = new ChitauriEnemy(0, 0);

        Attack attack = new RangedAttackStrategy().createAttack(enemy);

        assertEquals(10, attack.damage());
    }

    @Test
    void heavyShouldDealIncreasedDamage() {
        Enemy enemy = new ChitauriEnemy(0, 0);

        Attack attack = new HeavyAttackStrategy().createAttack(enemy);

        assertEquals(18, attack.damage());
    }

    @Test
    void strategyShouldRejectNullEnemy() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MeleeAttackStrategy().createAttack(null)
        );
    }
}