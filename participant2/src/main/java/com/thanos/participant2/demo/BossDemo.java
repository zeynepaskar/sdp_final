package com.thanos.participant2.demo;

import com.thanos.participant2.enemies.BossType;
import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.factory.BossEnemyFactory;

public class BossDemo {

    public static void main(String[] args) {

        for (BossType type : BossType.values()) {

            BossEnemyFactory factory = new BossEnemyFactory();

            Enemy boss = factory.create(type, 100, 100);

            System.out.println("--------------------");
            System.out.println("Boss: " + type.getDisplayName());
            System.out.println("Stone: " + type.stone().displayName());
            System.out.println("HP: " + boss.health());
            System.out.println("Damage: " + boss.getDamage());
        }
    }
}
