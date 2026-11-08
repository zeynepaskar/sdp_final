package com.thanos.participant2.demo;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.factory.BasicEnemyFactory;
import com.thanos.participant2.factory.BossEnemyFactory;
import com.thanos.participant2.factory.EnemyFactory;
import com.thanos.participant2.enemies.BossType;

public class FactoryDemo {

    public static void main(String[] args) {

        EnemyFactory basicFactory = new BasicEnemyFactory();
        BossEnemyFactory bossFactory =
                new BossEnemyFactory();

        Enemy enemy = basicFactory.create(100, 200);
        Enemy boss = bossFactory.create(BossType.CHAMPION, 300, 400);

        System.out.println("Enemy HP: " + enemy.health());
        System.out.println("Boss HP: " + boss.health());

        enemy.takeDamage(30);
        boss.takeDamage(100);

        System.out.println("Enemy HP after damage: " + enemy.health());
        System.out.println("Boss HP after damage: " + boss.health());
    }
}
