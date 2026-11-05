package com.thanos.participant1.characters;

import com.thanos.participant1.levels.World;

/** Player position and feet-based movement through the tile map. */
public final class Thanos {
    public static final float WIDTH = 30f, HEIGHT = 42f, FEET_HEIGHT = 16f, SPEED = 240f;
    private float x;
    private float y;

    public Thanos(float x, float y) { spawnAt(x, y); }
    public float x() { return x; }
    public float y() { return y; }
    public float width() { return WIDTH; }
    public float height() { return HEIGHT; }

    public void spawnAt(float x, float y) {
        if (!Float.isFinite(x) || !Float.isFinite(y) || x < 0 || y < 0) {
            throw new IllegalArgumentException("Spawn must be finite and nonnegative");
        }
        this.x = x;
        this.y = y;
    }

    public void move(float horizontal, float vertical, float delta, World world) {
        if (!Float.isFinite(horizontal) || !Float.isFinite(vertical) || !Float.isFinite(delta) || delta < 0) {
            throw new IllegalArgumentException("Movement needs finite input and nonnegative time");
        }
        x = clamp(x, world.width() - WIDTH);
        y = clamp(y, world.height() - HEIGHT);
        double distance = SPEED * (double) delta / Math.max(1, Math.hypot(horizontal, vertical));
        float targetX = clamp(x + horizontal * distance, world.width() - WIDTH);
        float targetY = clamp(y + vertical * distance, world.height() - HEIGHT);
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(targetX - x), Math.abs(targetY - y)) / 8));
        float stepX = (targetX - x) / steps;
        float stepY = (targetY - y) / steps;
        for (int step = 0; step < steps; step++) {
            float nextX = clamp(x + stepX, world.width() - WIDTH);
            if (world.layout().isWalkable(nextX, y, WIDTH, FEET_HEIGHT)) {
                x = nextX;
            }
            float nextY = clamp(y + stepY, world.height() - HEIGHT);
            if (world.layout().isWalkable(x, nextY, WIDTH, FEET_HEIGHT)) {
                y = nextY;
            }
        }
    }

    private static float clamp(double value, float maximum) {
        return (float) Math.max(0, Math.min(value, maximum));
    }
}
