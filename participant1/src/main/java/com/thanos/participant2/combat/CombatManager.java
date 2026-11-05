
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

    // Thanos attacks the nearest enemy within range
    public Enemy playerAttack(
            float playerX,
            float playerY,
            float attackRange,
            float damage) {

        if (!Float.isFinite(playerX)
                || !Float.isFinite(playerY)
                || !Float.isFinite(attackRange)
                || !Float.isFinite(damage)
                || attackRange < 0
                || damage < 0) {
            throw new IllegalArgumentException("Invalid attack parameters");
        }

        Enemy target = null;
        double closestDistance = Double.MAX_VALUE;

        // Player dimensions: 30 x 42
        float playerCenterX = playerX + 15;
        float playerCenterY = playerY + 21;

        // Enemy render dimensions: 28 x 32
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }

            float enemyCenterX = enemy.getX() + 14;
            float enemyCenterY = enemy.getY() + 16;

            double distance = Math.hypot(
                    playerCenterX - enemyCenterX,
                    playerCenterY - enemyCenterY
            );

            if (distance <= attackRange && distance < closestDistance) {
                closestDistance = distance;
                target = enemy;
            }
        }

        if (target != null) {
            damageEnemy(target, damage);
        }

        return target;
    }

}
