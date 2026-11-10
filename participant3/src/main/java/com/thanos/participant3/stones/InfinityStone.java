package com.thanos.participant3.stones;

import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.decorator.*;
import java.util.function.UnaryOperator;

/** The six stones with their colour, decorator and passive text. */
public enum InfinityStone {
    SOUL("Soul Stone", 0xF39C12FF, "Heals Thanos for part of the damage", SoulStoneDecorator::new),
    POWER("Power Stone", 0x8E44ADFF, "Raises damage", PowerStoneDecorator::new),
    TIME("Time Stone", 0x27AE60FF, "Weakens the enemy's counterattack", TimeStoneDecorator::new),
    SPACE("Space Stone", 0x2E86DEFF, "Makes attacks unblockable", SpaceStoneDecorator::new),
    REALITY("Reality Stone", 0xE74C3CFF, "Turns enemy defense into damage", RealityStoneDecorator::new),
    MIND("Mind Stone", 0xF1C40FFF, "Stuns the enemy", MindStoneDecorator::new);

    private final String displayName;
    private final int rgba;
    private final String effect;
    private final UnaryOperator<Attack> decorator;

    InfinityStone(String displayName, int rgba, String effect, UnaryOperator<Attack> decorator) {
        this.displayName = displayName;
        this.rgba = rgba;
        this.effect = effect;
        this.decorator = decorator;
    }

    public String displayName() { return displayName; }
    public int rgba() { return rgba; }
    public String effect() { return effect; }

    public static InfinityStone fromDisplayName(String name) {
        for (InfinityStone stone : values()) {
            if (stone.displayName.equalsIgnoreCase(name)) return stone;
        }
        throw new IllegalArgumentException("Unknown stone: " + name);
    }

    public String passiveName() {
        return switch (this) {
            case SOUL -> "Harvest";
            case POWER -> "Might";
            case TIME -> "Quickening";
            case SPACE -> "Portal";
            case REALITY -> "Bent Rules";
            case MIND -> "Foresight";
        };
    }

    public String passive() {
        return passiveName() + ": " + switch (this) {
            case SOUL -> "each victory heals " + SoulHarvest.HEAL_PERCENT + "% of health";
            case POWER -> "every blow deals " + (Inventory.POWER_DAMAGE_PERCENT - 100) + "% more damage";
            case TIME -> "+" + Inventory.TIME_ENERGY_BONUS + " energy every turn";
            case SPACE -> SpacePortal.CHANCE_PERCENT + "% of enemy blows miss, "
                    + SpacePortal.RETURN_PERCENT + "% of them hit back";
            case REALITY -> "abilities cost " + Inventory.REALITY_DISCOUNT + " less energy";
            case MIND -> "shows how hard the enemy will hit next";
        };
    }

    public Attack empower(Attack attack) { return decorator.apply(attack); }
}
