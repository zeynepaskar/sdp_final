package com.thanos.participant1.patterns.builder;

import com.thanos.participant1.levels.World;
import com.thanos.participant1.levels.WorldLayout;

/** Fluent construction keeps world configuration readable and produces immutable worlds. */
public final class WorldBuilder {
    private String id = "world", name = "World", subtitle = "";
    private float width = 1120, height = 600;
    private float spawnX = 90, spawnY = 270;
    private float portalX = 1000, portalY = 240, portalWidth = 56, portalHeight = 110;
    private int background = 0xC6CBA3FF, accent = 0x8D8C9CFF;

    public WorldBuilder id(String id) { this.id = id; return this; }
    public WorldBuilder name(String name) { this.name = name; return this; }
    public WorldBuilder subtitle(String subtitle) { this.subtitle = subtitle; return this; }
    public WorldBuilder size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }
    public WorldBuilder spawn(float x, float y) { spawnX = x; spawnY = y; return this; }
    public WorldBuilder portal(float x, float y, float width, float height) {
        portalX = x;
        portalY = y;
        portalWidth = width;
        portalHeight = height;
        return this;
    }
    public WorldBuilder colors(int background, int accent) { this.background = background; this.accent = accent; return this; }

    public World build() {
        WorldLayout layout = WorldLayout.forWorld(id, width, height, spawnX, spawnY, portalX, portalY);
        return new World(id, name, subtitle, width, height, spawnX, spawnY,
                portalX, portalY, portalWidth, portalHeight, background, accent, layout);
    }
}
