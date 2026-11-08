package com.thanos.participant2.enemies;

import com.thanos.participant3.Combatant;

public abstract class Enemy implements Combatant {

    // Identity
    private final String name;

    // Combat statistics
    private int health;
    private final int maxHealth;
    private final int damage;
    private final float speed;
    private final int defense;

    // Position
    private float x;
    private float y;
    protected Enemy(
            String name,
            int health,
            int damage,
            float speed,
            int defense,
            float x,
            float y
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Enemy name is required");
        }

        if (health <= 0 || damage < 0 || speed < 0
                || defense < 0 || defense > 80
                || !Float.isFinite(speed)
                || !Float.isFinite(x)
                || !Float.isFinite(y)) {
            throw new IllegalArgumentException("Invalid enemy statistics");
        }

        this.name = name;
        this.health = health;
        this.maxHealth = health;
        this.damage = damage;
        this.speed = speed;
        this.defense = defense;
        this.x = x;
        this.y = y;
    }
    @Override
    public String name() {
        return name;
    }

    @Override
    public int health() {
        return health;
    }

    @Override
    public int maxHealth() {
        return maxHealth;
    }

    @Override
    public int defense() {
        return defense;
    }

    @Override
    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Damage cannot be negative"
            );
        }

        health = Math.max(0, health - amount);
    }

    @Override
    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Healing cannot be negative"
            );
        }

        health = Math.min(maxHealth, health + amount);
    }
    public int getDamage() {
        return damage;
    }

    public float getSpeed() {
        return speed;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPosition(float x, float y) {
        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            throw new IllegalArgumentException("Invalid position");
        }

        this.x = x;
        this.y = y;
    }
}