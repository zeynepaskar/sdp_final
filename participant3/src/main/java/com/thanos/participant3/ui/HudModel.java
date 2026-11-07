package com.thanos.participant3.ui;

import com.thanos.participant3.Combatant;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.observer.GameEvent;
import com.thanos.participant3.patterns.observer.GameEventListener;
import com.thanos.participant3.stones.InfinityStone;
import java.util.*;

/** Observer: HUD snapshot that changes only on events. */
public final class HudModel implements GameEventListener {
    private final Combatant hero;
    private final Inventory inventory;
    private final int feedSize;
    private final Deque<String> feed = new ArrayDeque<>();
    private int health, maxHealth, energy, maxEnergy;
    private Set<InfinityStone> stones = Set.of();

    public HudModel(Combatant hero, Inventory inventory, int feedSize) {
        if (hero == null || inventory == null || feedSize < 1) {
            throw new IllegalArgumentException("HUD needs a hero, an inventory and room for at least one event");
        }
        this.hero = hero;
        this.inventory = inventory;
        this.feedSize = feedSize;
        refresh();
    }

    public void onEvent(GameEvent event) {
        refresh();
        feed.addFirst(event.message());
        while (feed.size() > feedSize) feed.removeLast();
    }

    public String heroName() { return hero.name(); }
    public int health() { return health; }
    public int maxHealth() { return maxHealth; }
    public int energy() { return energy; }
    public int maxEnergy() { return maxEnergy; }
    public boolean hasStone(InfinityStone stone) { return stones.contains(stone); }
    public int stoneCount() { return stones.size(); }
    public List<String> feed() { return List.copyOf(feed); }

    private void refresh() {
        health = hero.health();
        maxHealth = hero.maxHealth();
        energy = inventory.energy();
        maxEnergy = inventory.maxEnergy();
        stones = inventory.stones();
    }
}
