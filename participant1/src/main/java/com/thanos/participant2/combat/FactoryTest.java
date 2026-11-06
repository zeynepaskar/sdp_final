
package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.factory.BasicEnemyFactory;
import com.thanos.participant2.factory.BossEnemyFactory;
import com.thanos.participant2.factory.EnemyFactory;
import com.thanos.participant2.enemies.BossType;

public class FactoryTest {

    public static void main(String[] args) {

        EnemyFactory basicFactory = new BasicEnemyFactory();
        EnemyFactory bossFactory =
                new BossEnemyFactory(BossType.CHAMPION);

        Enemy enemy = basicFactory.spawnEnemy(100, 200);
        Enemy boss = bossFactory.spawnEnemy(300, 400);

        System.out.println("Enemy HP: " + enemy.getHealth());
        System.out.println("Boss HP: " + boss.getHealth());

        enemy.takeDamage(30);
        boss.takeDamage(100);

        System.out.println("Enemy HP after damage: " + enemy.getHealth());
        System.out.println("Boss HP after damage: " + boss.getHealth());
    }
}

