package com.thanos.participant3.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.thanos.participant3.stones.Ability;
import com.thanos.participant3.stones.AbilityBook;
import com.thanos.participant3.stones.InfinityStone;
import java.util.List;
import java.util.function.Function;

/** Renders StoneRewards with a ShapeRenderer. */
public final class StoneRewardView implements Disposable {
    private static final float CENTER_X = 640, CENTER_Y = 500;
    private static final float GEM_RADIUS = 70;
    private static final float SLOT_RADIUS = 6;
    private static final int RAYS = 12;
    private static final int SPARKS = 10;
    private final StoneRewards rewards;
    private final Function<InfinityStone, Vector2> target;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Color stoneColor = new Color();
    private final Color color = new Color();

    public StoneRewardView(StoneRewards rewards, Function<InfinityStone, Vector2> target) {
        if (rewards == null || target == null) {
            throw new IllegalArgumentException("Stone reward view needs the rewards and a target");
        }
        this.rewards = rewards;
        this.target = target;
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public void render(Matrix4 projection) {
        InfinityStone stone = rewards.current();
        if (stone == null) return;
        stoneColor.set(stone.rgba());
        float p = rewards.phaseProgress(), time = rewards.time();
        float dim, glow, x = CENTER_X, y = CENTER_Y, radius;
        switch (rewards.phase()) {
            case APPEAR -> {
                dim = p;
                glow = p;
                radius = GEM_RADIUS * easeOutBack(p);
            }
            case SHOW -> {
                dim = 1;
                glow = 1;
                radius = GEM_RADIUS * (1 + 0.05f * MathUtils.sin(time * 6));
            }
            default -> {
                float eased = p * p * (3 - 2 * p);
                Vector2 slot = target.apply(stone);
                dim = 1 - eased;
                glow = 1 - Math.min(1, p * 2);
                x = MathUtils.lerp(CENTER_X, slot.x, eased);
                y = MathUtils.lerp(CENTER_Y, slot.y, eased) + MathUtils.sin(eased * MathUtils.PI) * 120;
                radius = MathUtils.lerp(GEM_RADIUS, SLOT_RADIUS, eased);
            }
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(color.set(Hud.INK).mul(0.6f, 0.6f, 0.6f, 0.85f * dim));
        shapes.rect(0, 0, 1280, 800);
        if (glow > 0) {
            drawRays(time, glow);
            drawRings(time, glow);
            drawSparks(time, glow);
        }
        if (rewards.phase() == StoneRewards.Phase.FLY) drawLanding(stone, p);
        drawGem(x, y, radius);
        shapes.end();

        if (rewards.phase() == StoneRewards.Phase.FLY) return;
        float alpha = rewards.phase() == StoneRewards.Phase.SHOW ? Math.min(1, p * 5) : 0;
        if (alpha == 0) return;
        List<InfinityStone> found = rewards.foundUpToCurrent();
        List<Ability> abilities = AbilityBook.abilities(found);
        String unlocked = abilities.get(abilities.size() - 1).name();
        batch.setProjectionMatrix(projection);
        batch.begin();
        float line = stone.passive().isEmpty() ? 212 : 180;
        centered(stone.displayName().toUpperCase(), 345, 3.4f, color.set(Hud.INK).mul(0.5f, 0.5f, 0.5f, alpha), 4);
        centered(stone.displayName().toUpperCase(), 345, 3.4f, color.set(stoneColor).mul(1, 1, 1, alpha), 0);
        centered("acquired   " + rewards.count() + " / 6", 290, 1.7f, color.set(Hud.PAPER).mul(1, 1, 1, alpha), 0);
        centered(stone.effect(), 245, 1.4f, color.set(Hud.PAPER).mul(1, 1, 1, alpha), 0);
        if (!stone.passive().isEmpty()) {
            centered("Passive  -  " + stone.passive(), 212, 1.4f, color.set(Hud.PAPER).mul(1, 1, 1, alpha), 0);
        }
        centered("New ability:  " + unlocked, line, 1.4f, color.set(stoneColor).mul(1, 1, 1, alpha), 0);
        if (!AbilityBook.upgradedByLast(found).isEmpty()) {
            centered("Gauntlet complete!  All eight skills are upgraded", line - 30, 1.3f,
                    color.set(Hud.PAPER).mul(1, 1, 1, alpha), 0);
        }
        if ((int) (time * 2) % 2 == 0) {
            centered("ENTER or click", 110, 1.2f, color.set(Hud.PAPER).mul(1, 1, 1, 0.6f * alpha), 0);
        }
        batch.end();
    }

    private void drawRays(float time, float glow) {
        shapes.setColor(color.set(stoneColor).mul(1, 1, 1, 0.22f * glow));
        float length = 300 * glow;
        for (int i = 0; i < RAYS; i++) {
            float angle = time * 20 + i * 360f / RAYS;
            shapes.triangle(CENTER_X, CENTER_Y,
                    CENTER_X + MathUtils.cosDeg(angle - 5) * length, CENTER_Y + MathUtils.sinDeg(angle - 5) * length,
                    CENTER_X + MathUtils.cosDeg(angle + 5) * length, CENTER_Y + MathUtils.sinDeg(angle + 5) * length);
        }
    }

    private void drawRings(float time, float glow) {
        for (int i = 0; i < 3; i++) {
            float spread = (time * 0.7f + i / 3f) % 1;
            float radius = GEM_RADIUS + 180 * spread;
            shapes.setColor(color.set(stoneColor).mul(1, 1, 1, 0.45f * (1 - spread) * glow));
            ring(CENTER_X, CENTER_Y, radius, 4);
        }
    }

    private void drawSparks(float time, float glow) {
        for (int i = 0; i < SPARKS; i++) {
            float angle = -time * 40 + i * 360f / SPARKS;
            float distance = 115 + 25 * MathUtils.sin(time * 2 + i);
            float size = 4 + 3 * Math.abs(MathUtils.sin(time * 5 + i * 1.7f));
            shapes.setColor(color.set(Color.WHITE).lerp(stoneColor, 0.4f).mul(1, 1, 1, glow));
            shapes.rect(CENTER_X + MathUtils.cosDeg(angle) * distance - size / 2,
                    CENTER_Y + MathUtils.sinDeg(angle) * distance - size / 2, size, size);
        }
    }

    private void drawLanding(InfinityStone stone, float p) {
        if (p < 0.8f) return;
        float flash = (p - 0.8f) / 0.2f;
        Vector2 slot = target.apply(stone);
        shapes.setColor(color.set(stoneColor).mul(1, 1, 1, 1 - flash));
        ring(slot.x, slot.y, 8 + 30 * flash, 3);
    }

    private void drawGem(float x, float y, float radius) {
        if (radius <= 0) return;
        float[] points = gemPoints(x, y, radius * 1.15f + 2);
        shapes.setColor(Hud.INK);
        fan(x, y, points);
        points = gemPoints(x, y, radius);
        shapes.setColor(stoneColor);
        fan(x, y, points);
        shapes.setColor(color.set(stoneColor).lerp(Color.WHITE, 0.45f));
        shapes.triangle(x, y, points[10], points[11], points[0], points[1]);
        shapes.triangle(x, y, points[8], points[9], points[10], points[11]);
        shapes.setColor(color.set(stoneColor).lerp(Color.BLACK, 0.25f));
        shapes.triangle(x, y, points[2], points[3], points[4], points[5]);
        shapes.triangle(x, y, points[4], points[5], points[6], points[7]);
        float shine = radius * 0.18f;
        shapes.setColor(Color.WHITE);
        shapes.rect(x - radius * 0.35f - shine / 2, y + radius * 0.3f - shine / 2, shine, shine);
    }

    private static float[] gemPoints(float x, float y, float r) {
        return new float[] {x, y + r, x + r * 0.75f, y + r * 0.45f, x + r * 0.75f, y - r * 0.45f,
                x, y - r, x - r * 0.75f, y - r * 0.45f, x - r * 0.75f, y + r * 0.45f};
    }

    private void fan(float x, float y, float[] points) {
        for (int i = 0; i < points.length; i += 2) {
            int next = (i + 2) % points.length;
            shapes.triangle(x, y, points[i], points[i + 1], points[next], points[next + 1]);
        }
    }

    private void ring(float x, float y, float radius, float width) {
        int segments = 48;
        for (int i = 0; i < segments; i++) {
            float a = i * 360f / segments, b = (i + 1) * 360f / segments;
            float ax = MathUtils.cosDeg(a), ay = MathUtils.sinDeg(a), bx = MathUtils.cosDeg(b), by = MathUtils.sinDeg(b);
            float inner = radius - width;
            shapes.triangle(x + ax * inner, y + ay * inner, x + ax * radius, y + ay * radius, x + bx * radius, y + by * radius);
            shapes.triangle(x + ax * inner, y + ay * inner, x + bx * radius, y + by * radius, x + bx * inner, y + by * inner);
        }
    }

    private void centered(String value, float y, float scale, Color tint, float shadow) {
        font.getData().setScale(scale);
        font.setColor(tint);
        layout.setText(font, value);
        font.draw(batch, value, CENTER_X - layout.width / 2 + shadow, y - shadow);
    }

    private static float easeOutBack(float p) {
        float c = 1.70158f, t = p - 1;
        return 1 + (c + 1) * t * t * t + c * t * t;
    }

    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
