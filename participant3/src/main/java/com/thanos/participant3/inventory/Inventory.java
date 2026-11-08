package com.thanos.participant3.inventory;

import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.DamageScaleDecorator;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEventType;
import com.thanos.participant3.stones.Ability;
import com.thanos.participant3.stones.AbilityBook;
import com.thanos.participant3.stones.InfinityStone;
import java.util.*;

/** Stones and energy: publishes every change, listens for LEVEL_UP and applies the Power, Time and Reality passives. */
public final class Inventory {
    public static final int BASE_MAX_ENERGY = 100;
    public static final int ENERGY_PER_LEVEL = 10;
    public static final int ENERGY_PER_TURN = 10;
    public static final int TIME_ENERGY_BONUS = 5;
    public static final int POWER_DAMAGE_PERCENT = 110;
    public static final int REALITY_DISCOUNT = 5;
    private static final String OWNER = "Thanos";
    private final EventBus events;
    private final EnumSet<InfinityStone> stones = EnumSet.noneOf(InfinityStone.class);
    private final List<InfinityStone> found = new ArrayList<>();
    private int energy;
    private int maxEnergy = BASE_MAX_ENERGY;

    public Inventory(EventBus events, int energy) {
        if (events == null || energy < 0 || energy > BASE_MAX_ENERGY) {
            throw new IllegalArgumentException("Inventory needs an event bus and energy within 0.." + BASE_MAX_ENERGY);
        }
        this.events = events;
        this.energy = energy;
        events.subscribe(event -> {
            if (event.type() == GameEventType.LEVEL_UP && OWNER.equals(event.source())) raiseMaxEnergy();
        });
    }

    public boolean addStone(InfinityStone stone) {
        if (stone == null) {
            throw new IllegalArgumentException("Stone must not be null");
        }
        if (!stones.add(stone)) return false;
        found.add(stone);
        events.publish(GameEventType.STONE_ACQUIRED, OWNER,
                OWNER + " acquired the " + stone.displayName() + " (" + stones.size() + "/6)", stones.size());
        return true;
    }

    public boolean has(InfinityStone stone) { return stones.contains(stone); }
    public Set<InfinityStone> stones() { return Collections.unmodifiableSet(stones.clone()); }
    public int stoneCount() { return stones.size(); }
    public boolean isComplete() { return stones.size() == InfinityStone.values().length; }
    public List<InfinityStone> stonesInOrder() { return List.copyOf(found); }
    public List<Ability> abilities() { return AbilityBook.abilities(found); }
    public int energy() { return energy; }
    public int maxEnergy() { return maxEnergy; }

    public int energyPerTurn() { return ENERGY_PER_TURN + (has(InfinityStone.TIME) ? TIME_ENERGY_BONUS : 0); }

    public int costOf(Ability ability) {
        if (ability == null) {
            throw new IllegalArgumentException("Ability must not be null");
        }
        return Math.max(0, ability.energyCost() - (has(InfinityStone.REALITY) ? REALITY_DISCOUNT : 0));
    }

    public Attack empower(Attack attack) {
        if (attack == null) {
            throw new IllegalArgumentException("Attack must not be null");
        }
        return has(InfinityStone.POWER) ? new DamageScaleDecorator(attack, POWER_DAMAGE_PERCENT) : attack;
    }

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
        setEnergy(Math.min(maxEnergy, energy + amount));
    }

    public Optional<Attack> use(Ability ability, Attack attack) {
        if (ability == null || attack == null) {
            throw new IllegalArgumentException("Ability and attack are required");
        }
        int cost = costOf(ability);
        if (!ability.isBase() && !has(ability.stone()) || !spendEnergy(cost)) return Optional.empty();
        events.publish(GameEventType.ABILITY_USED, OWNER, OWNER + " used " + ability.name()
                + (ability.isBase() ? "" : " (" + ability.stone().displayName() + ")"), cost);
        return Optional.of(ability.apply(attack));
    }

    private void raiseMaxEnergy() {
        maxEnergy += ENERGY_PER_LEVEL;
        setEnergy(energy + ENERGY_PER_LEVEL);
    }

    private void setEnergy(int value) {
        if (value == energy) return;
        energy = value;
        events.publish(GameEventType.ENERGY_CHANGED, OWNER, OWNER + " energy: " + energy + "/" + maxEnergy, energy);
    }
}
