package com.thanos.participant1.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.thanos.participant1.characters.PlayerView;
import com.thanos.participant1.levels.World;
import com.thanos.participant1.levels.WorldLayout;
import com.thanos.participant1.levels.WorldLayout.Tile;

import static com.thanos.participant1.levels.WorldLayout.TILE_SIZE;

final class MapArt implements Disposable {
    static final int LEFT = 80, BOTTOM = 100;
    private static final int SOUTH = 0, WEST = 1, EAST = 2, NORTH = 3;
    private static final int[] STEP = {1, 0, 1, 2};
    private static final int[] EDGE_X = {0, 1, 0, -1};
    private static final int[] EDGE_Y = {1, 0, -1, 0};

    private final Texture terrain = load("terrain.png");
    private final Texture walker = load("thanos-walk.png");
    private final TextureRegion[][] tiles = TextureRegion.split(terrain, 20, 20);
    private final TextureRegion[][] poses = TextureRegion.split(walker, 32, 40);
    private final TextureRegion closedGate = new TextureRegion(terrain, 5, 45, 30, 30);
    private final TextureRegion openGate = new TextureRegion(terrain, 45, 45, 30, 30);
    private final Color groundTint = new Color(), canopyTint = new Color();
    private World lastWorld;
    private float previousX = Float.NaN, previousY;
    private int facingRow;

    private static Texture load(String file) {
        Texture texture = new Texture(Gdx.files.classpath("art/map/" + file));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    void drawTerrain(SpriteBatch batch, World world, float elapsed) {
        if (lastWorld != world) {
            groundTint.set(world.backgroundRgba()).lerp(Color.WHITE, .4f);
            canopyTint.set(world.accentRgba()).lerp(Color.WHITE, .5f);
            lastWorld = world;
            previousX = Float.NaN;
        }
        WorldLayout layout = world.layout();
        int wave = (int) (elapsed * 2) & 1;
        for (int row = 0; row < layout.rows(); row++) {
            for (int column = 0; column < layout.columns(); column++) {
                drawTile(batch, layout, column, row, wave);
            }
        }
        batch.setColor(Color.WHITE);
    }

    void drawGate(SpriteBatch batch, World world, boolean gateOpen) {
        batch.draw(gateOpen ? openGate : closedGate, LEFT + world.portalX() - 8,
                BOTTOM + world.portalY(), world.portalWidth() + 16, world.portalHeight());
    }

    private void drawTile(SpriteBatch batch, WorldLayout layout, int column, int row, int wave) {
        Tile tile = layout.tileAt(column, row);
        float x = LEFT + column * TILE_SIZE, y = BOTTOM + row * TILE_SIZE;
        int variant = (column * 17 + row * 5 + (row ^ (column / 2))) % 3;
        if (tile == Tile.STONEWORK || tile == Tile.BOARDWALK) {
            int materialRow = tile == Tile.STONEWORK ? 4 : 5;
            batch.setColor(groundTint);
            batch.draw(tiles[materialRow][4 + variant], x, y, TILE_SIZE, TILE_SIZE);
            return;
        }
        int frame = switch (tile) {
            case PATH -> 3 + variant;
            case WATER -> 6 + ((row + wave) & 1);
            default -> variant;
        };
        batch.setColor(groundTint);
        batch.draw(tiles[0][frame], x, y, TILE_SIZE, TILE_SIZE);
        if (tile == Tile.PATH || tile == Tile.WATER) {
            TextureRegion[] edges = tiles[tile == Tile.PATH ? 4 : 5];
            for (int side = 0; side < EDGE_X.length; side++) {
                int nx = column + EDGE_X[side], ny = row + EDGE_Y[side];
                if (nx >= 0 && nx < layout.columns() && ny >= 0 && ny < layout.rows()
                        && layout.tileAt(nx, ny) != tile) {
                    batch.draw(edges[side], x, y, TILE_SIZE, TILE_SIZE);
                }
            }
        } else if (tile == Tile.TREE || tile == Tile.ROCK) {
            batch.setColor(tile == Tile.TREE ? canopyTint : groundTint);
            TextureRegion prop = tiles[1][tile == Tile.TREE ? 0 : 1];
            boolean mirror = (row + column) % 3 == 0;
            batch.draw(prop, mirror ? x + TILE_SIZE : x, y, mirror ? -TILE_SIZE : TILE_SIZE, TILE_SIZE);
        }
    }

    void drawPlayer(SpriteBatch batch, PlayerView player, float elapsed) {
        float dx = player.x() - previousX, dy = player.y() - previousY;
        boolean moving = (dx != 0 || dy != 0) && Math.abs(dx) + Math.abs(dy) < TILE_SIZE;
        if (moving) {
            if (Math.abs(dx) > Math.abs(dy)) facingRow = dx > 0 ? EAST : WEST;
            else facingRow = dy > 0 ? NORTH : SOUTH;
        } else if (Float.isNaN(previousX) || Math.abs(dx) + Math.abs(dy) >= TILE_SIZE) {
            facingRow = SOUTH;
        }
        previousX = player.x();
        previousY = player.y();
        int frame = moving ? STEP[(int) (elapsed * 9) % STEP.length] : 1;
        batch.draw(poses[facingRow][frame], LEFT + Math.round(player.x() + player.width() / 2) - 16,
                BOTTOM + Math.round(player.y()), 32, 40);
    }

    TextureRegion alert() { return tiles[1][2]; }
    TextureRegion facing(int clockwiseFromNorth) { return tiles[1][3 + clockwiseFromNorth]; }

    @Override
    public void dispose() {
        walker.dispose();
        terrain.dispose();
    }
}
