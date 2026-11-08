package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.stones.Ability;
import com.thanos.participant3.stones.AbilityBook;
import com.thanos.participant3.stones.InfinityStone;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class AbilityBookTest {
    private static final List<Ability> ALL = AbilityBook.forStones(EnumSet.allOf(InfinityStone.class));

    private static Attack punchWith(String name) {
        Ability ability = ALL.stream().filter(a -> a.name().equals(name)).findFirst().orElseThrow();
        return ability.apply(new BasicAttack("Punch", 60));
    }

    @Test
    void everyStoneUnlocksThreeAbilities() {
        for (InfinityStone stone : InfinityStone.values()) {
            List<Ability> abilities = AbilityBook.forStone(stone);
            assertEquals(3, abilities.size());
            abilities.forEach(ability -> assertEquals(stone, ability.stone()));
        }
        assertEquals(18, ALL.stream().map(Ability::name).distinct().count());
    }

    @Test
    void strongerVersionsStackTheirStone() {
        assertEquals(60, punchWith("Life Siphon").lifestealPercent());
        assertEquals(135, punchWith("Energy Blast").damage());
        assertEquals(202, punchWith("Planet Breaker").damage());
        assertEquals(50, punchWith("Rapid Decay").slowPercent());
        assertEquals(2, punchWith("Mind Control").stunTurns());
    }

    @Test
    void specialAbilitiesMixInNewEffects() {
        Attack mend = punchWith("Soul Mend");
        assertEquals(30, mend.damage());
        assertEquals(150, mend.selfHeal());
        assertEquals(30, punchWith("Rewind").damage());
        assertEquals(100, punchWith("Rewind").rewindPercent());
        Attack portal = punchWith("Portal Strike");
        assertEquals(105, portal.damage());
        assertTrue(portal.unblockable());
        assertEquals(40, punchWith("Gravity Well").slowPercent());
        Attack distortion = punchWith("Distortion");
        assertEquals(90, distortion.damage());
        assertTrue(distortion.turnsDefenseIntoDamage());
        assertEquals(50, punchWith("Transmutation").slowPercent());
        Attack spike = punchWith("Mind Spike");
        assertEquals(90, spike.damage());
        assertEquals(1, spike.stunTurns());
    }

    @Test
    void soulMendHealsTheAttacker() {
        FakeCombatant thanos = new FakeCombatant("Thanos", 500, 1000, 10);
        FakeCombatant champion = new FakeCombatant("Champion", 800, 800, 10);

        new HitResolver(new EventBus()).resolve(thanos, champion, punchWith("Soul Mend"));

        assertEquals(773, champion.health());
        assertEquals(650, thanos.health());
    }
}
