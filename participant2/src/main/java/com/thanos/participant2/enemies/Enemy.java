
package com.thanos.participant2.enemies;

/**
 * Base class for all enemies in the game.
 */
public abstract class Enemy {

    private float x;
    private float y;

    private final float maxHealth;
    private float health;

    private final float damage;
    private final float speed;

    public Enemy(float x, float y,
                 float health, float damage, float speed) {

        if (health <= 0 || damage < 0 || speed < 0) {
            throw new IllegalArgumentException(
                    "Enemy statistics must be valid"
            );
        }

        this.x = x;
        this.y = y;

        this.maxHealth = health;
        this.health = health;

        this.damage = damage;
        this.speed = speed;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getDamage() {
        return damage;
    }

    public float getSpeed() {
        return speed;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(float amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Damage cannot be negative"
            );
        }

        health = Math.max(0, health - amount);
    }

    public void move(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }
}
