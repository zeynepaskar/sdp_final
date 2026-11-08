package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.patterns.decorator.*;
import com.thanos.participant3.patterns.observer.EventBus;
import org.junit.jupiter.api.Test;

class AttackDecoratorTest {
    private final Attack punch = new BasicAttack("Punch", 50);

    @Test
    void powerStoneRaisesDamageAndExtendsDescription() {
        Attack empowered = new PowerStoneDecorator(punch);

        assertEquals(75, empowered.damage());
        assertEquals("Punch (50) + Power Stone (x1.5 damage)", empowered.description());
    }

    @Test
    void stonesStackWithoutAClassPerCombination() {
        Attack combo = new RealityStoneDecorator(new TimeStoneDecorator(new PowerStoneDecorator(punch)));

        assertEquals(75, combo.damage());
        assertTrue(combo.ignoresDefense());
        assertEquals(TimeStoneDecorator.SLOW_PERCENT, combo.slowPercent());
        assertFalse(combo.unblockable());
        assertEquals("Punch (50) + Power Stone (x1.5 damage) + Time Stone (counterattack -25%)"
                + " + Reality Stone (ignores defense)", combo.description());
    }

    @Test
    void decoratorsKeepOtherEffectsOfTheWrappedAttack() {
        Attack combo = new MindStoneDecorator(new SpaceStoneDecorator(new SoulStoneDecorator(punch)));

        assertEquals(50, combo.damage());
        assertEquals(SoulStoneDecorator.LIFESTEAL_PERCENT, combo.lifestealPercent());
        assertTrue(combo.unblockable());
        assertEquals(1, combo.stunTurns());
    }

    @Test
    void realityStoneIgnoresDefense() {
        HitResolver resolver = new HitResolver(new EventBus());
        FakeCombatant thanos = new FakeCombatant("Thanos", 1000, 1000, 0);

        assertEquals(35, resolver.resolve(thanos, new FakeCombatant("Champion", 500, 500, 30), punch));
        assertEquals(50, resolver.resolve(thanos, new FakeCombatant("Champion", 500, 500, 30),
                new RealityStoneDecorator(punch)));
    }

    @Test
    void soulStoneHealsTheAttacker() {
        FakeCombatant thanos = new FakeCombatant("Thanos", 850, 1000, 0);

        new HitResolver(new EventBus()).resolve(thanos, new FakeCombatant("Gardener", 1800, 1800, 0),
                new SoulStoneDecorator(new BasicAttack("Strike", 100)));

        assertEquals(880, thanos.health());
    }

    @Test
    void attackNeedsSomethingToWrap() {
        assertThrows(IllegalArgumentException.class, () -> new PowerStoneDecorator(null));
        assertThrows(IllegalArgumentException.class, () -> new BasicAttack("Punch", -1));
    }
}
