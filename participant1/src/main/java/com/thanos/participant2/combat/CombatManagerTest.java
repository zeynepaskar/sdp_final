
package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.enemies.BasicEnemy;
import com.thanos.participant2.enemies.BossEnemy;
import com.thanos.participant2.strategy.HeavyAttackStrategy;
import com.thanos.participant2.strategy.MeleeAttackStrategy;

public class CombatManagerTest {

    public static void main(String[] args) {

        CombatManager manager = new CombatManager();

        // Create enemies
        Enemy enemy = new BasicEnemy(100, 100);
        Enemy boss = new BossEnemy(200, 200);

        // Add enemies to the game
        manager.addEnemy(enemy);
        manager.addEnemy(boss);

        System.out.println("Enemies: " + manager.getEnemyCount());

        // Thanos attacks the basic enemy
        manager.damageEnemy(enemy, 30);

        System.out.println("Enemy HP: " + enemy.getHealth());

        // Boss attacks Thanos using Heavy Strategy
        float damage = manager.enemyAttack(
                boss,
                201,
                200,
                new HeavyAttackStrategy()
        );

        System.out.println("Boss attack damage: " + damage);

        // Defeat the basic enemy
        manager.damageEnemy(enemy, 100);

        System.out.println("Enemies after defeat: "
                + manager.getEnemyCount());
    }
}