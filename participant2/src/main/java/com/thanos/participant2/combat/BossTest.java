
package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.BossType;
import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.factory.BossEnemyFactory;
import com.thanos.participant2.factory.EnemyFactory;

public class BossTest {

    public static void main(String[] args) {

        for (BossType type : BossType.values()) {

            EnemyFactory factory = new BossEnemyFactory(type);

            Enemy boss = factory.spawnEnemy(100, 100);

            System.out.println("--------------------");
            System.out.println("Boss: " + type.getName());
            System.out.println("Stone: " + type.getInfinityStone());
            System.out.println("HP: " + boss.getHealth());
            System.out.println("Damage: " + boss.getDamage());
        }
    }
}
