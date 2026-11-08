package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.stones.Ability;
import com.thanos.participant3.stones.AbilityBook;
import com.thanos.participant3.stones.InfinityStone;
import com.thanos.participant3.stones.Skill;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class AbilityBookTest {
    private static Attack punchWith(String name) {
        Ability ability = AbilityBook.allSkills().stream().flatMap(skill -> Stream.of(skill.basic(), skill.upgraded()))
                .filter(a -> a.name().equals(name)).findFirst().orElseThrow();
        return ability.apply(new BasicAttack("Punch", 60));
    }

    private static List<String> names(List<InfinityStone> stones) {
        return AbilityBook.abilities(stones).stream().map(Ability::name).toList();
    }

    @Test
    void twoBaseSkillsAndOnePerStone() {
        List<Skill> skills = AbilityBook.allSkills();

        assertEquals(8, skills.size());
        assertTrue(skills.get(0).basic().isBase());
        assertTrue(skills.get(1).basic().isBase());
        for (InfinityStone stone : InfinityStone.values()) {
            assertEquals(1, skills.stream().filter(skill -> skill.stone() == stone).count());
        }
        assertEquals(16, skills.stream().flatMap(skill -> Stream.of(skill.basic(), skill.upgraded()))
                .map(Ability::name).distinct().count());
    }

    @Test
    void eachStoneUnlocksOneSkillInTheOrderFound() {
        assertEquals(List.of("Punch", "Titan's Fury"), names(List.of()));
        assertEquals(List.of("Punch", "Titan's Fury", "Mind Blast"), names(List.of(InfinityStone.MIND)));
        assertEquals(List.of("Punch", "Titan's Fury", "Mind Blast", "Soul Drain", "Rewind"),
                names(List.of(InfinityStone.MIND, InfinityStone.SOUL, InfinityStone.TIME)));
        assertEquals(List.of(), AbilityBook.upgradedByLast(List.of(InfinityStone.SPACE)));
    }

    @Test
    void theFullGauntletUpgradesEverything() {
        List<InfinityStone> all = List.of(InfinityStone.values());

        assertEquals(List.of("Crushing Blow", "Titan's Wrath", "Life Siphon", "Planet Breaker", "Time Loop",
                "Gravity Well", "Reality Rewrite", "Mind Spike"), names(all));
        assertEquals(AbilityBook.allSkills(), AbilityBook.upgradedByLast(all));
        assertEquals(List.of("Punch", "Titan's Fury", "Soul Drain", "Energy Blast", "Rewind", "Portal Strike",
                "Distortion"), names(all.subList(0, 5)));
    }

    @Test
    void baseSkillsScaleThePunch() {
        assertEquals(60, punchWith("Punch").damage());
        assertEquals(75, punchWith("Crushing Blow").damage());
        assertEquals(90, punchWith("Titan's Fury").damage());
        assertEquals(120, punchWith("Titan's Wrath").damage());
    }

    @Test
    void powerOutgrowsTheTitan() {
        assertEquals(135, punchWith("Energy Blast").damage());
        assertEquals(180, punchWith("Planet Breaker").damage());
        assertTrue(punchWith("Energy Blast").damage() > punchWith("Titan's Wrath").damage());
    }

    @Test
    void upgradesAddAnEffectOnTop() {
        assertEquals(30, punchWith("Soul Drain").lifestealPercent());
        assertEquals(60, punchWith("Life Siphon").lifestealPercent());
        assertEquals(30, punchWith("Rewind").damage());
        assertEquals(100, punchWith("Rewind").rewindPercent());
        assertEquals(100, punchWith("Time Loop").rewindPercent());
        assertEquals(25, punchWith("Time Loop").slowPercent());
        assertEquals(105, punchWith("Portal Strike").damage());
        assertTrue(punchWith("Portal Strike").unblockable());
        assertEquals(40, punchWith("Gravity Well").slowPercent());
        assertTrue(punchWith("Distortion").turnsDefenseIntoDamage());
        assertEquals(90, punchWith("Reality Rewrite").damage());
        assertEquals(1, punchWith("Mind Blast").stunTurns());
        assertEquals(1, punchWith("Mind Spike").stunTurns());
        assertEquals(90, punchWith("Mind Spike").damage());
    }

    @Test
    void soulDrainHealsTheAttacker() {
        FakeCombatant thanos = new FakeCombatant("Thanos", 500, 1000, 10);
        FakeCombatant champion = new FakeCombatant("Champion", 800, 800, 10);

        new HitResolver(new EventBus()).resolve(thanos, champion, punchWith("Soul Drain"));

        assertEquals(746, champion.health());
        assertEquals(516, thanos.health());
    }
}
