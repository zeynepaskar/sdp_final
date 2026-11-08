package com.thanos.participant3.stones;

import static com.thanos.participant3.stones.InfinityStone.*;

import com.thanos.participant3.patterns.decorator.DamageScaleDecorator;
import com.thanos.participant3.patterns.decorator.RewindDecorator;
import com.thanos.participant3.patterns.decorator.SelfHealDecorator;
import com.thanos.participant3.patterns.decorator.WeakenDecorator;
import java.util.*;

/** Catalogue of three decorator-built abilities per stone. */
public final class AbilityBook {
    private static final Map<InfinityStone, List<Ability>> ABILITIES = new EnumMap<>(Map.of(
            SOUL, List.of(
                    new Ability("Soul Drain", SOUL, 20, "Strike that feeds on the enemy's life"),
                    new Ability("Life Siphon", SOUL, 35, "Drains twice as much: heals 60% of the damage",
                            attack -> SOUL.empower(SOUL.empower(attack))),
                    new Ability("Soul Mend", SOUL, 30, "A light blow that restores 150 health",
                            attack -> new SelfHealDecorator(new DamageScaleDecorator(attack, 50), 150))),
            POWER, List.of(
                    new Ability("Cosmic Strike", POWER, 25, "A blow with the raw force of the universe"),
                    new Ability("Energy Blast", POWER, 40, "The Power Stone twice over: more than double damage",
                            attack -> POWER.empower(POWER.empower(attack))),
                    new Ability("Planet Breaker", POWER, 60, "The blow that shattered Tamarata: triple damage",
                            attack -> POWER.empower(POWER.empower(POWER.empower(attack))))),
            TIME, List.of(
                    new Ability("Aging", TIME, 20, "Ages the enemy so it hits back weaker"),
                    new Ability("Rapid Decay", TIME, 35, "Ages the enemy fast: its next hit is 50% weaker",
                            attack -> TIME.empower(TIME.empower(attack))),
                    new Ability("Rewind", TIME, 35, "Undoes the enemy's last blow and strikes at half strength",
                            attack -> new RewindDecorator(new DamageScaleDecorator(attack, 50), 100))),
            SPACE, List.of(
                    new Ability("Spatial Rift", SPACE, 20, "Attacks through a rift that cannot be blocked"),
                    new Ability("Portal Strike", SPACE, 35, "Strikes from behind through a portal: +75% damage",
                            attack -> new DamageScaleDecorator(SPACE.empower(attack), 175)),
                    new Ability("Gravity Well", SPACE, 30, "Crushes the enemy: its next hit is 40% weaker",
                            attack -> new WeakenDecorator(SPACE.empower(attack), 40))),
            REALITY, List.of(
                    new Ability("Law Breaker", REALITY, 30, "Turns the enemy's defense into extra damage"),
                    new Ability("Distortion", REALITY, 45, "Bends reality: +50%, defense becomes damage",
                            attack -> new DamageScaleDecorator(REALITY.empower(attack), 150)),
                    new Ability("Transmutation", REALITY, 35, "Turns part of the enemy to stone: next hit -50%",
                            attack -> new WeakenDecorator(REALITY.empower(attack), 50))),
            MIND, List.of(
                    new Ability("Mind Blast", MIND, 30, "Stuns the enemy for a turn"),
                    new Ability("Mind Spike", MIND, 45, "Pierces the mind: +50% damage and a stun",
                            attack -> new DamageScaleDecorator(MIND.empower(attack), 150)),
                    new Ability("Mind Control", MIND, 55, "The enemy obeys and loses two turns",
                            attack -> MIND.empower(MIND.empower(attack))))));

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
