package com.thanos.participant3.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.thanos.participant3.stones.InfinityStone;
import java.util.List;

/**
 * Draws the HudModel in a strip along the bottom of a 1280x800 view, in the same paper-and-ink look as the menus.
 * Call render with the game camera's combined matrix after the world is drawn.
 */
public final class Hud implements Disposable {
    public static final Color INK = Color.valueOf("353B33");
    public static final Color PAPER = Color.valueOf("F2EDD0");
    private static final Color EMPTY = Color.valueOf("D8D2B0");
    private static final Color HEALTH = Color.valueOf("C0392B");
    private static final Color ENERGY = Color.valueOf("2E86DE");
    private static final int MAX_LINE = 85;
    private final HudModel model;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Color color = new Color();

    public Hud(HudModel model) {
        if (model == null) {
            throw new IllegalArgumentException("HUD needs a model");
        }
        this.model = model;
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        font.setColor(INK);
    }

    public void render(Matrix4 projection) {
        shapes.setProjectionMatrix(projection);
        batch.setProjectionMatrix(projection);

        // Everything stays below y = 100, where the game's world starts.
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        panel(16, 8, 560, 90);
        panel(592, 8, 672, 90);
        bar(130, 54, 170, 12, model.health(), model.maxHealth(), HEALTH);
        bar(130, 30, 170, 12, model.energy(), model.maxEnergy(), ENERGY);
        InfinityStone[] stones = InfinityStone.values();
        for (int i = 0; i < stones.length; i++) {
            float x = 330 + i * 38, y = 24;
            shapes.setColor(INK);
            shapes.rect(x, y, 30, 30);
            shapes.setColor(model.hasStone(stones[i]) ? color.set(stones[i].rgba()) : EMPTY);
            shapes.rect(x + 3, y + 3, 24, 24);
        }
        shapes.end();

        batch.begin();
        font.getData().setScale(1.2f);
        font.draw(batch, model.heroName().toUpperCase(), 32, 90);
        font.draw(batch, "EVENTS", 608, 90);
        font.getData().setScale(1f);
        font.draw(batch, "HP " + model.health() + "/" + model.maxHealth(), 32, 66);
        font.draw(batch, "Energy " + model.energy() + "/" + model.maxEnergy(), 32, 42);
        font.draw(batch, "Stones " + model.stoneCount() + "/6", 330, 90);
        List<String> feed = model.feed();
        for (int i = 0; i < Math.min(3, feed.size()); i++) {
            String line = feed.get(i);
            if (line.length() > MAX_LINE) line = line.substring(0, MAX_LINE - 3) + "...";
            font.draw(batch, (i == 0 ? "> " : "  ") + line, 608, 68 - i * 18);
        }
        batch.end();
    }

    private void panel(float x, float y, float width, float height) {
        shapes.setColor(INK);
        shapes.rect(x, y, width, height);
        shapes.setColor(PAPER);
        shapes.rect(x + 3, y + 3, width - 6, height - 6);
    }

    private void bar(float x, float y, float width, float height, int value, int max, Color fill) {
        shapes.setColor(INK);
        shapes.rect(x, y, width, height);
        shapes.setColor(EMPTY);
        shapes.rect(x + 2, y + 2, width - 4, height - 4);
        float share = max <= 0 ? 0 : Math.max(0, Math.min(1, value / (float) max));
        shapes.setColor(fill);
        shapes.rect(x + 2, y + 2, (width - 4) * share, height - 4);
    }

    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
