package com.thanos.participant1.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.thanos.participant1.characters.PlayerView;
import com.thanos.participant1.levels.Encounter;
import com.thanos.participant1.levels.World;
import com.thanos.participant1.levels.WorldLayout;
import com.thanos.participant1.patterns.facade.GameFacade;

import com.thanos.participant1.patterns.facade.GamePhase;
import com.thanos.participant3.ui.PixelSprite;
import com.thanos.participant3.ui.Sprites;

final class WorldRenderer {
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Color color = new Color();
    private float previousX = Float.NaN, previousY = Float.NaN;

    void setProjection(Matrix4 projection) {
        shapes.setProjectionMatrix(projection);
    }

    void render(GameFacade facade, float elapsed) {
        World world = facade.world();
        int grass = world.backgroundRgba();
        int leaves = world.accentRgba();
        int shade = mix(leaves, 0x353B33FF, .55f);
        int path = mix(grass, 0xEAD2A2FF, .72f);
        int grassBlade = mix(grass, leaves, .38f);
        int water = mix(leaves, 0x8BBAD1FF, .74f);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int row = 0; row < world.layout().rows(); row++) {
            for (int column = 0; column < world.layout().columns(); column++) {
                float x = column * WorldLayout.TILE_SIZE;
                float y = row * WorldLayout.TILE_SIZE;
                rect(grass, x, y, 40, 40);
                switch (world.layout().tileAt(column, row)) {
                    case PATH -> rect(path, x, y, 40, 40);
                    case GRASS -> {
                        rect(grassBlade, x + 8, y + 10, 3, 8);
                        rect(grassBlade, x + 12, y + 7, 3, 8);
                    }
                    case TREE -> {
                        rect(shade, x + 16, y, 8, 12);
                        rect(shade, x + 4, y + 8, 32, 28);
                        rect(leaves, x + 8, y + 12, 24, 24);
                    }
                    case ROCK -> {
                        rect(shade, x + 4, y + 4, 32, 28);
                        rect(0xBABAA8FF, x + 8, y + 12, 20, 20);
                    }
                    case WATER -> {
                        rect(water, x, y, 40, 40);
                        rect(0xD2E1D4FF, x + 8, y + 12, 20, 3);
                    }
                }
            }
        }
        rect(shade, world.portalX() - 8, world.portalY(), world.portalWidth() + 16, world.portalHeight());
        rect(0xDAC59BFF, world.portalX() - 12, world.portalY() + world.portalHeight() - 12, world.portalWidth() + 24, 12);
        rect(leaves, world.portalX() + 8, world.portalY(), world.portalWidth() - 16, world.portalHeight() - 24);
        if (facade.exitUnlocked()) {
            rect(0xF8E4A3FF, world.portalX() + 8, world.portalY(), world.portalWidth() - 16, world.portalHeight() - 20);
        } else {
            for (int i = 8; i < world.portalWidth(); i += 10) {
                rect(shade, world.portalX() + i, world.portalY(), 4, world.portalHeight() - 12);
            }
        }
        for (Encounter encounter : world.encounters()) {
            if (!facade.isCleared(encounter)) drawEnemy(encounter);
        }
        drawPlayer(facade.player(), elapsed);
        if (facade.phase() == GamePhase.ALERT) drawAlert(facade.activeEncounter());
        shapes.end();
    }

    private void drawEnemy(Encounter enemy) {
        float cx = enemy.x() + 20, cy = enemy.y() + 19;
        PixelSprite sprite = Sprites.forEnemy(enemy.kind().displayName());
        float scale = enemy.isBoss() ? 2.25f : 1.75f;
        sprite.draw(shapes, 80 + cx - sprite.width() * scale / 2, 100 + enemy.y() + 3, scale);
        int dx = enemy.facing().dx(), dy = enemy.facing().dy();
        float x = 80 + cx + 24 * dx, y = 100 + cy + 24 * dy;
        shapes.setColor(color.set(0x4F3947FF));
        shapes.triangle(x + dx * 5, y + dy * 5, x - dx * 3 + dy * 5, y - dy * 3 - dx * 5,
                x - dx * 3 - dy * 5, y - dy * 3 + dx * 5);
    }

    private void drawAlert(Encounter enemy) {
        float x = enemy.x() + 14, y = enemy.y() + 45;
        rect(0x353B33FF, x - 2, y - 2, 16, 25);
        rect(0xFFF1CCFF, x, y, 12, 21);
        rect(0x9B3F49FF, x + 4, y + 8, 4, 10);
        rect(0x9B3F49FF, x + 4, y + 3, 4, 3);
    }

    private void drawPlayer(PlayerView player, float elapsed) {
        float x = Math.round(player.x());
        float y = Math.round(player.y());
        boolean moving = player.x() != previousX || player.y() != previousY;
        float step = moving && (int) (elapsed * 9) % 2 == 0 ? 3 : 0;
        previousX = player.x();
        previousY = player.y();
        rect(0x40384DFF, x + 3, y + step, 9, 12);
        rect(0x40384DFF, x + 18, y + 3 - step, 9, 12);
        rect(0x694F81FF, x, y + 9, 30, 24);
        rect(0xD7B15FFF, x, y + 9, 30, 6);
        rect(0xD7B15FFF, x + 24, y + 15, 9, 15);
        rect(0xBB96CCFF, x + 6, y + 27, 18, 12);
        rect(0x40384DFF, x + 9, y + 33, 3, 3);
        rect(0x40384DFF, x + 18, y + 33, 3, 3);
    }

    private int mix(int first, int second, float amount) {
        return Color.rgba8888(color.set(first).lerp(new Color(second), amount));
    }

    private void rect(int rgba, float x, float y, float width, float height) {
        shapes.setColor(color.set(rgba));
        shapes.rect(80 + x, 100 + y, width, height);
    }

    public void dispose() {
        shapes.dispose();
    }
}
