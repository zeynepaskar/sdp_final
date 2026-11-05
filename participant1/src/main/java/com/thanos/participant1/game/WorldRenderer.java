package com.thanos.participant1.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.thanos.participant1.levels.*;
import com.thanos.participant1.patterns.facade.GameFacade;
import com.thanos.participant1.characters.PlayerView;

/** Draws the same tiles used for collision; world colors come from the Builder. */
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
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int row = 0; row < world.layout().rows(); row++) {
            for (int column = 0; column < world.layout().columns(); column++) {
                float x = column * WorldLayout.TILE_SIZE;
                float y = row * WorldLayout.TILE_SIZE;
                rect(grass, x, y, 40, 40);
                switch (world.layout().tileAt(column, row)) {
                    case PATH -> rect(mix(grass, 0xEAD2A2FF, .72f), x, y, 40, 40);
                    case GRASS -> {
                        rect(mix(grass, leaves, .38f), x + 8, y + 10, 3, 8);
                        rect(mix(grass, leaves, .38f), x + 12, y + 7, 3, 8);
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
                        rect(mix(leaves, 0x8BBAD1FF, .74f), x, y, 40, 40);
                        rect(0xD2E1D4FF, x + 8, y + 12, 20, 3);
                    }
                }
            }
        }
        rect(shade, world.portalX() - 8, world.portalY(), world.portalWidth() + 16, world.portalHeight());
        rect(0xDAC59BFF, world.portalX() - 12, world.portalY() + world.portalHeight() - 12, world.portalWidth() + 24, 12);
        rect(leaves, world.portalX() + 8, world.portalY(), world.portalWidth() - 16, world.portalHeight() - 24);
        drawPlayer(facade.player(), elapsed);
        shapes.end();
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
