package com.thanos.participant3.stones;

import static com.thanos.participant3.stones.InfinityStone.*;

import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.DamageScaleDecorator;
import com.thanos.participant3.patterns.decorator.RewindDecorator;
import com.thanos.participant3.patterns.decorator.WeakenDecorator;
import java.util.*;

/**
 * Thanos's eight skills: two base ones and one per stone. Each new stone unlocks its skill; the sixth stone
 * completes the gauntlet and upgrades all eight skills at once.
 */
public final class AbilityBook {
    private static final List<Skill> BASE = List.of(
            new Skill(new Ability("Punch", null, 0, "A plain blow", attack -> attack),
                    new Ability("Crushing Blow", null, 0, "A heavier blow: +25% damage",
                            attack -> new DamageScaleDecorator(attack, 125))),
            new Skill(new Ability("Titan's Fury", null, 25, "Raw Titan strength: +50% damage",
                            attack -> new DamageScaleDecorator(attack, 150)),
                    new Ability("Titan's Wrath", null, 20, "Double damage",
                            attack -> new DamageScaleDecorator(attack, 200))));

    private static final Map<InfinityStone, Skill> STONES = new EnumMap<>(Map.of(
            SOUL, new Skill(
                    new Ability("Soul Drain", SOUL, 20, "Heals Thanos for 30% of the damage"),
                    new Ability("Life Siphon", SOUL, 30, "Heals Thanos for 60% of the damage",
                            attack -> SOUL.empower(SOUL.empower(attack)))),
            POWER, new Skill(
                    new Ability("Energy Blast", POWER, 40, "The Power Stone twice over: x2.25 damage",
                            attack -> POWER.empower(POWER.empower(attack))),
                    new Ability("Planet Breaker", POWER, 50, "Triple damage",
                            attack -> POWER.empower(new DamageScaleDecorator(attack, 200)))),
            TIME, new Skill(
                    new Ability("Rewind", TIME, 35, "Undoes the enemy's last blow, strikes at half strength",
                            AbilityBook::rewind),
                    new Ability("Time Loop", TIME, 35, "Rewind, and the enemy's next hit is 25% weaker",
                            attack -> TIME.empower(rewind(attack)))),
            SPACE, new Skill(
                    new Ability("Portal Strike", SPACE, 35, "Ignores defense: +75% damage",
                            AbilityBook::portalStrike),
                    new Ability("Gravity Well", SPACE, 35, "Portal Strike, and the enemy's next hit is 40% weaker",
                            attack -> new WeakenDecorator(portalStrike(attack), 40))),
            REALITY, new Skill(
                    new Ability("Distortion", REALITY, 30, "Turns the enemy's defense into extra damage"),
                    new Ability("Reality Rewrite", REALITY, 40, "Defense becomes damage, then +50%",
                            attack -> new DamageScaleDecorator(REALITY.empower(attack), 150))),
            MIND, new Skill(
                    new Ability("Mind Blast", MIND, 30, "Stuns the enemy: it loses its next turn"),
                    new Ability("Mind Spike", MIND, 40, "Stun and +50% damage",
                            attack -> new DamageScaleDecorator(MIND.empower(attack), 150)))));

    private AbilityBook() { }

    private static Attack rewind(Attack attack) {
        return new RewindDecorator(new DamageScaleDecorator(attack, 50), 100);
    }

    private static Attack portalStrike(Attack attack) {
        return new DamageScaleDecorator(SPACE.empower(attack), 175);
    }

    public static List<Skill> baseSkills() { return BASE; }

    /** All eight skills: the base ones, then one per stone in story order. */
    public static List<Skill> allSkills() { return unlocked(List.of(InfinityStone.values())); }

    public static Skill forStone(InfinityStone stone) {
        if (stone == null) {
            throw new IllegalArgumentException("Stone must not be null");
        }
        return STONES.get(stone);
    }

    /** Skills in unlock order: the two base skills, then one per stone in the order the stones were found. */
    public static List<Skill> unlocked(List<InfinityStone> stonesInOrder) {
        List<Skill> skills = new ArrayList<>(BASE);
        for (InfinityStone stone : stonesInOrder) skills.add(forStone(stone));
        return List.copyOf(skills);
    }

    /** How many skills (counted in unlock order) are upgraded once this many stones are found. */
    public static int upgradedCount(int stoneCount) {
        return stoneCount >= InfinityStone.values().length ? BASE.size() + stoneCount : 0;
    }

    /** The abilities Thanos can use with these stones, each in its plain or upgraded form. */
    public static List<Ability> abilities(List<InfinityStone> stonesInOrder) {
        List<Skill> skills = unlocked(stonesInOrder);
        int upgraded = upgradedCount(stonesInOrder.size());
        List<Ability> result = new ArrayList<>();
        for (int i = 0; i < skills.size(); i++) result.add(skills.get(i).form(i < upgraded));
        return List.copyOf(result);
    }

    /** Skills that the last stone in the list upgraded. */
    public static List<Skill> upgradedByLast(List<InfinityStone> stonesInOrder) {
        if (stonesInOrder.isEmpty()) return List.of();
        List<Skill> skills = unlocked(stonesInOrder);
        int now = upgradedCount(stonesInOrder.size()), before = upgradedCount(stonesInOrder.size() - 1);
        return skills.subList(before, now);
    }
}
