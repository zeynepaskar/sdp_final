package com.thanos.participant3.stones;

import java.util.*;

/** One ability per stone for now; the story plans three each. */
public final class AbilityBook {
    private static final Map<InfinityStone, List<Ability>> ABILITIES = new EnumMap<>(Map.of(
            InfinityStone.SOUL, List.of(new Ability("Soul Drain", InfinityStone.SOUL, 20,
                    "Strike that feeds on the enemy's life")),
            InfinityStone.POWER, List.of(new Ability("Cosmic Strike", InfinityStone.POWER, 25,
                    "A blow with the raw force of the universe")),
            InfinityStone.TIME, List.of(new Ability("Aging", InfinityStone.TIME, 20,
                    "Ages the enemy so it hits back weaker")),
            InfinityStone.SPACE, List.of(new Ability("Spatial Rift", InfinityStone.SPACE, 20,
                    "Attacks through a rift that cannot be blocked")),
            InfinityStone.REALITY, List.of(new Ability("Law Breaker", InfinityStone.REALITY, 30,
                    "Switches off the enemy's defense for one hit")),
            InfinityStone.MIND, List.of(new Ability("Mind Blast", InfinityStone.MIND, 30,
                    "Stuns the enemy for a turn"))));

    private AbilityBook() { }

    public static List<Ability> forStone(InfinityStone stone) {
        if (stone == null) {
            throw new IllegalArgumentException("Stone must not be null");
        }
        return ABILITIES.get(stone);
    }

    public static List<Ability> forStones(Collection<InfinityStone> stones) {
        List<Ability> result = new ArrayList<>();
        for (InfinityStone stone : InfinityStone.values()) {
            if (stones.contains(stone)) result.addAll(forStone(stone));
        }
        return List.copyOf(result);
    }
}
