package com.thanos.participant3.progress;

import com.thanos.participant3.Combatant;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEventType;

/** Combatant with experience and levels; publishes LEVEL_UP. */
public final class Thanos implements Combatant {
    public static final String NAME = "Thanos";
    public static final int MAX_LEVEL = 7;
    public static final int BASE_HEALTH = 1000;
    public static final int HEALTH_PER_LEVEL = 100;
    public static final int BASE_DEFENSE = 10;
    public static final int DEFENSE_PER_LEVEL = 2;
    public static final int BASE_PUNCH = 60;
    public static final int PUNCH_PER_LEVEL = 10;
    private final EventBus events;
    private int level = 1;
    private int experience;
    private int health = BASE_HEALTH;

    public Thanos(EventBus events) {
        if (events == null) {
            throw new IllegalArgumentException("Thanos needs an event bus");
        }
        this.events = events;
    }

    public static int experienceFor(Combatant enemy) {
        if (enemy == null) {
            throw new IllegalArgumentException("Enemy must not be null");
        }
        return enemy.maxHealth() / 3;
    }

    public String name() { return NAME; }
    public int health() { return health; }
    public int maxHealth() { return BASE_HEALTH + HEALTH_PER_LEVEL * (level - 1); }
    public int defense() { return BASE_DEFENSE + DEFENSE_PER_LEVEL * (level - 1); }
    public int punchDamage() { return BASE_PUNCH + PUNCH_PER_LEVEL * (level - 1); }
    public int level() { return level; }
    public int experience() { return experience; }
    public int experienceToNextLevel() { return 50 * level; }

    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage must be nonnegative");
        }
        health = Math.max(0, health - amount);
    }

    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Healing must be nonnegative");
        }
        health = Math.min(maxHealth(), health + amount);
    }

    public void gainExperience(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Experience must be nonnegative");
        }
        if (amount == 0 || level == MAX_LEVEL) return;
        experience += amount;
        events.publish(GameEventType.XP_GAINED, NAME, NAME + " gained " + amount + " XP", amount);
        while (level < MAX_LEVEL && experience >= experienceToNextLevel()) {
            experience -= experienceToNextLevel();
            level++;
            health += HEALTH_PER_LEVEL;
            events.publish(GameEventType.LEVEL_UP, NAME, NAME + " reached level " + level + "! HP +"
                    + HEALTH_PER_LEVEL + ", defense +" + DEFENSE_PER_LEVEL + ", punch +" + PUNCH_PER_LEVEL
                    + ", energy +" + Inventory.ENERGY_PER_LEVEL, level);
            events.publish(GameEventType.HEALTH_CHANGED, NAME, NAME + " health: " + health + "/" + maxHealth(), health);
        }
        if (level == MAX_LEVEL) experience = 0;
    }
}
