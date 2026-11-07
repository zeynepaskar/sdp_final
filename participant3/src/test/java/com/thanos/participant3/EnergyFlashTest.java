package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.ui.EnergyFlash;
import org.junit.jupiter.api.Test;

class EnergyFlashTest {
    private final EnergyFlash flash = new EnergyFlash();

    @Test
    void splitsTheGainIntoRegularAndBonus() {
        flash.gained(40, 55, 10, 1000);

        assertEquals(40, flash.from());
        assertEquals(10, flash.regular());
        assertEquals(5, flash.bonus());
    }

    @Test
    void aNearlyFullBarCutsTheBonusFirst() {
        flash.gained(93, 100, 10, 1000);

        assertEquals(7, flash.regular());
        assertEquals(0, flash.bonus());
    }

    @Test
    void blinksForAWhileThenStops() {
        flash.gained(40, 50, 10, 1000);

        assertTrue(flash.active(1000));
        assertTrue(flash.bright(1000));
        assertFalse(flash.bright(1000 + EnergyFlash.BLINK_MS));
        assertTrue(flash.active(1000 + EnergyFlash.DURATION_MS - 1));
        assertFalse(flash.active(1000 + EnergyFlash.DURATION_MS));
    }

    @Test
    void noGainNoBlink() {
        assertFalse(flash.active(0));
        flash.gained(40, 50, 10, 1000);

        flash.gained(100, 100, 10, 1200);

        assertFalse(flash.active(1200));
    }
}
