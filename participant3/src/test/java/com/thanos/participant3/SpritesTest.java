package com.thanos.participant3;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.math.Vector2;
import com.thanos.participant3.stones.InfinityStone;
import com.thanos.participant3.ui.BattleScreen;
import com.thanos.participant3.ui.CharacterSprites;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpritesTest {
    private TextureAtlasData atlas;
    private BufferedImage sheet;

    @BeforeEach
    void readPackagedArtwork() throws Exception {
        var resource = CharacterSprites.class.getResource("/" + CharacterSprites.ATLAS);
        assertNotNull(resource);
        FileHandle atlasFile = new FileHandle(new File(resource.toURI()));
        atlas = new TextureAtlasData(atlasFile, atlasFile.parent(), false);
        sheet = ImageIO.read(atlas.getPages().first().textureFile.file());
    }

    @Test
    void allTenFightersHaveTheirOwnFrame() {
        Set<String> names = new HashSet<>();
        Set<String> positions = new HashSet<>();
        for (var region : atlas.getRegions()) {
            assertTrue(names.add(region.name));
            assertTrue(positions.add(region.left + "," + region.top));
            assertEquals(64, region.width);
            assertEquals(64, region.height);
        }
        assertEquals(Set.of("Thanos", "Outrider", "Chitauri", "Sakaaran", "In-Betweener",
                "Champion", "Gardener", "Runner", "Collector", "Grandmaster"), names);
    }

    @Test
    void framesContainVisiblePixelsAndTransparentSpace() {
        for (var region : atlas.getRegions()) {
            int opaque = 0;
            for (int y = region.top; y < region.top + region.height; y++) {
                for (int x = region.left; x < region.left + region.width; x++) {
                    if ((sheet.getRGB(x, y) >>> 24) > 0) opaque++;
                }
            }
            assertTrue(opaque > 0 && opaque < region.width * region.height, region.name);
        }
    }

    @Test
    void pixelsStaySharpAtBothZoomLevels() {
        var page = atlas.getPages().first();
        assertEquals(TextureFilter.Nearest, page.minFilter);
        assertEquals(TextureFilter.Nearest, page.magFilter);
    }

    @Test
    void eachCollectedStoneHasItsOwnLandingPoint() {
        var thanos = atlas.getRegions().first();
        assertEquals("Thanos", thanos.name);
        Set<Vector2> slots = new HashSet<>();
        for (InfinityStone stone : InfinityStone.values()) {
            assertTrue(slots.add(BattleScreen.gauntletSlot(stone)), stone.name());
            var socket = CharacterSprites.socket(stone);
            for (int dy = 0; dy < 2; dy++) {
                for (int dx = 0; dx < 2; dx++) {
                    assertEquals(0xFF755034, sheet.getRGB(thanos.left + socket.x() + dx,
                            thanos.top + socket.yFromTop() + dy), stone.name());
                }
            }
        }
        assertEquals(6, slots.size());
    }
}
