package com.thanos.participant3.inventory;

import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEventType;
import com.thanos.participant3.stones.Ability;
import com.thanos.participant3.stones.AbilityBook;
import com.thanos.participant3.stones.InfinityStone;
import java.util.*;

/** Thanos's stones and cosmic energy; every change is announced on the event bus. */
public final class Inventory {
    public static final int MAX_ENERGY = 100;
    private static final String OWNER = "Thanos";
    private final EventBus events;
    private final EnumSet<InfinityStone> stones = EnumSet.noneOf(InfinityStone.class);
    private int energy;

    public Inventory(EventBus events, int energy) {
        if (events == null || energy < 0 || energy > MAX_ENERGY) {
            throw new IllegalArgumentException("Inventory needs an event bus and energy within 0.." + MAX_ENERGY);
        }
        this.events = events;
        this.energy = energy;
    }

    /** Returns false if Thanos already has this stone. */
    public boolean addStone(InfinityStone stone) {
        if (stone == null) {
            throw new IllegalArgumentException("Stone must not be null");
        }
        if (!stones.add(stone)) return false;
        events.publish(GameEventType.STONE_ACQUIRED, OWNER,
                OWNER + " acquired the " + stone.displayName() + " (" + stones.size() + "/6)", stones.size());
        return true;
    }

    public boolean has(InfinityStone stone) { return stones.contains(stone); }
    public Set<InfinityStone> stones() { return Collections.unmodifiableSet(stones.clone()); }
    public int stoneCount() { return stones.size(); }
    public boolean isComplete() { return stones.size() == InfinityStone.values().length; }
    public List<Ability> abilities() { return AbilityBook.forStones(stones); }
    public int energy() { return energy; }

    /** Returns false and changes nothing if there is not enough energy. */
    public boolean spendEnergy(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be nonnegative");
        }
        if (amount > energy) return false;
        setEnergy(energy - amount);
        return true;
    }

    public void restoreEnergy(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be nonnegative");
        }
        setEnergy(Math.min(MAX_ENERGY, energy + amount));
    }

    /**
     * Uses an ability on top of an attack: needs the stone and enough energy.
     * Returns the decorated attack, or empty if the ability cannot be used now.
     */
    public Optional<Attack> use(Ability ability, Attack attack) {
        if (ability == null || attack == null) {
            throw new IllegalArgumentException("Ability and attack are required");
        }
        if (!has(ability.stone()) || !spendEnergy(ability.energyCost())) return Optional.empty();
        events.publish(GameEventType.ABILITY_USED, OWNER,
                OWNER + " used " + ability.name() + " (" + ability.stone().displayName() + ")", ability.energyCost());
        return Optional.of(ability.apply(attack));
    }

    private void setEnergy(int value) {
        if (value == energy) return;
        energy = value;
        events.publish(GameEventType.ENERGY_CHANGED, OWNER, OWNER + " energy: " + energy + "/" + MAX_ENERGY, energy);
    }
}
