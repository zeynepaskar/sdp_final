package com.thanos.participant3.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** One pixel-art battle backdrop for every world; only the tint changes. */
public final class Backdrop {
    private static final float WIDTH = 1280, HEIGHT = 800, HORIZON = 560, CELL = 8;
    private static final int[] RIDGE = {3, 4, 6, 7, 6, 5, 4, 4, 5, 7, 9, 8, 6, 5, 3, 2, 3, 4, 5, 4,
            3, 3, 4, 6, 8, 9, 7, 5, 4, 3, 4, 5, 6, 5, 4, 3, 2, 3, 4, 3};
    private static final int[] STARS = {36, 770, 118, 730, 212, 782, 300, 748, 410, 772, 470, 724, 760, 700,
            830, 650, 900, 730, 980, 700, 1060, 736, 1150, 690, 1240, 720, 1200, 640, 700, 600, 1100, 610,
            60, 640, 20, 590, 560, 600};
    /** Ground marks as x, y, width. */
    private static final int[] MARKS = {60, 520, 48, 300, 528, 48, 560, 512, 48, 1180, 520, 48,
            20, 440, 64, 470, 448, 64, 1200, 430, 64, 120, 352, 80, 560, 360, 80, 600, 232, 96, 1240, 224, 40};

    private final Color sky, haze, star, planet, planetLight, hills, ground, mark, shadow;

    public Backdrop(int tintRgba) {
        Color tint = new Color(tintRgba);
        sky = shade(tint, 0.14f);
        haze = shade(tint, 0.22f);
        star = shade(tint, 0.55f);
        planet = shade(tint, 0.42f);
        planetLight = shade(tint, 0.28f);
        hills = shade(tint, 0.36f);
        ground = shade(tint, 0.20f);
        mark = shade(tint, 0.30f);
        shadow = shade(tint, 0.36f);
    }

    private static Color shade(Color tint, float amount) { return Hud.PAPER.cpy().lerp(tint, amount); }

    /** Colour for the ellipses the fighters stand on. */
    public Color shadow() { return shadow; }

    public void draw(ShapeRenderer shapes) {
        shapes.setColor(sky);
        shapes.rect(0, HORIZON, WIDTH, HEIGHT - HORIZON);
        shapes.setColor(haze);
        shapes.rect(0, HORIZON, WIDTH, 6 * CELL);
        shapes.setColor(star);
        for (int i = 0; i < STARS.length; i += 2) shapes.rect(STARS[i], STARS[i + 1], CELL / 2, CELL / 2);
        disc(shapes, 640, 700, 48, planet);
        disc(shapes, 632, 708, 32, planetLight);
        shapes.setColor(hills);
        float column = WIDTH / RIDGE.length;
        for (int i = 0; i < RIDGE.length; i++) shapes.rect(i * column, HORIZON, column, RIDGE[i] * CELL);
        shapes.setColor(ground);
        shapes.rect(0, 0, WIDTH, HORIZON);
        shapes.setColor(mark);
        for (int i = 0; i < MARKS.length; i += 3) shapes.rect(MARKS[i], MARKS[i + 1], MARKS[i + 2], CELL);
    }

    /** A circle built from CELL-high rows so it matches the sprites. */
    private static void disc(ShapeRenderer shapes, float cx, float cy, float r, Color color) {
        shapes.setColor(color);
        for (float dy = -r; dy < r; dy += CELL) {
            float mid = dy + CELL / 2;
            float half = Math.round((float) Math.sqrt(r * r - mid * mid) / CELL) * CELL;
            shapes.rect(cx - half, cy + dy, 2 * half, CELL);
        }
    }
}
