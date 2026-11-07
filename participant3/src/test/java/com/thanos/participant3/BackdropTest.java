package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.graphics.Color;
import com.thanos.participant3.stones.InfinityStone;
import com.thanos.participant3.ui.Backdrop;
import com.thanos.participant3.ui.Hud;
import org.junit.jupiter.api.Test;

class BackdropTest {
    @Test
    void eachWorldTintsTheSameBackdrop() {
        Color soul = new Backdrop(InfinityStone.SOUL.rgba()).shadow();
        Color time = new Backdrop(InfinityStone.TIME.rgba()).shadow();

        assertNotEquals(soul, time);
        assertEquals(soul, new Backdrop(InfinityStone.SOUL.rgba()).shadow());
        assertEquals(Color.valueOf("F2EDD0"), Hud.PAPER, "shading must not change the shared paper colour");
    }
}
