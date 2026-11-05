package com.thanos.participant1.levels;

import com.thanos.participant1.characters.Thanos;

/** Immutable world assembled by WorldBuilder. */
public record World(String id, String name, String subtitle, float width, float height,
                    float spawnX, float spawnY, float portalX, float portalY,
                    float portalWidth, float portalHeight, int backgroundRgba, int accentRgba,
                    WorldLayout layout) {
    public World {
        if (id == null || id.isBlank() || name == null || name.isBlank() || subtitle == null) {
            throw new IllegalArgumentException("World needs an id, name and subtitle");
        }
        if (!fits(spawnX, spawnY, Thanos.WIDTH, Thanos.HEIGHT, width, height)
                || !fits(portalX, portalY, portalWidth, portalHeight, width, height)) {
            throw new IllegalArgumentException("Spawn and portal must fit inside a finite, positive world");
        }
        if (layout == null || layout.width() != width || layout.height() != height
                || !layout.isWalkable(spawnX, spawnY, Thanos.WIDTH, Thanos.FEET_HEIGHT)) {
            throw new IllegalArgumentException("Layout must match the world and have a walkable spawn");
        }
    }

    public boolean overlapsPortal(float x, float y, float width, float height) {
        return Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(width) && Float.isFinite(height)
                && width > 0 && height > 0 && x < portalX + portalWidth && x + width > portalX
                && y < portalY + portalHeight && y + height > portalY;
    }

    private static boolean fits(float x, float y, float w, float h, float worldW, float worldH) {
        return Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(w) && Float.isFinite(h)
                && Float.isFinite(worldW) && Float.isFinite(worldH) && w > 0 && h > 0
                && worldW > 0 && worldH > 0 && x >= 0 && y >= 0 && x + w <= worldW && y + h <= worldH;
    }
}
