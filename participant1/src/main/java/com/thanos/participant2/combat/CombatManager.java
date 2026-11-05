
package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.strategy.CombatStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CombatManager {

    private final List<Enemy> enemies = new ArrayList<>();

    // Add a new enemy to the game
    public void addEnemy(Enemy enemy) {
        if (enemy != null) {
            enemies.add(enemy);
        }
    }

    // Get all enemies
    public List<Enemy> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    // Enemy attacks a target using a selected strategy
    public float enemyAttack(
            Enemy enemy,
            float targetX,
            float targetY,
            CombatStrategy strategy) {

        if (enemy == null || strategy == null || !enemy.isAlive()) {
            return 0;
        }

        return strategy.attack(enemy, targetX, targetY);
    }

    // Thanos attacks an enemy
    public void damageEnemy(Enemy enemy, float damage) {
        if (enemy == null || !enemies.contains(enemy)) {
            return;
        }

        if (enemy.isAlive()) {
            enemy.takeDamage(damage);
        }

        removeDeadEnemies();
    }

    // Remove defeated enemies
    public void removeDeadEnemies() {
        enemies.removeIf(enemy -> !enemy.isAlive());
    }

    public int getEnemyCount() {
        return enemies.size();
    }
}
