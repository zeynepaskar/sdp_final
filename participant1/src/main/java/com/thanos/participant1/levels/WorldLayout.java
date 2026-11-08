package com.thanos.participant1.levels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorldLayout {
    public record Cell(int column, int row) { }
    private final Map<Character, Cell> markers = new HashMap<>();
    public static final float TILE_SIZE = 40f;
    public enum Tile {
        GRASS, PATH, TREE, WATER, ROCK;
        public boolean isWalkable() { return this == GRASS || this == PATH; }
    }
    private final float width;
    private final float height;
    private final Tile[][] tiles;

    private WorldLayout(float width, float height) {
        if (!Float.isFinite(width) || !Float.isFinite(height) || width <= 0 || height <= 0
                || Math.ceil(width / TILE_SIZE) * Math.ceil(height / TILE_SIZE) > 1_000_000) {
            throw new IllegalArgumentException("Map needs positive dimensions and at most one million tiles");
        }
        this.width = width;
        this.height = height;
        tiles = new Tile[rows()][columns()];
    }

    public float width() { return width; }
    public float height() { return height; }
    public int columns() { return (int) Math.ceil(width / TILE_SIZE); }
    public int rows() { return (int) Math.ceil(height / TILE_SIZE); }
    public Tile tileAt(int column, int row) { return tiles[row][column]; }

    public boolean isWalkable(float x, float y, float w, float h) {
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(w) || !Float.isFinite(h)
                || w <= 0 || h <= 0 || x < 0 || y < 0 || (double) x + w > width || (double) y + h > height) {
            return false;
        }
        for (int row = (int) (y / TILE_SIZE); row < Math.ceil(((double) y + h) / TILE_SIZE); row++) {
            for (int column = (int) (x / TILE_SIZE); column < Math.ceil(((double) x + w) / TILE_SIZE); column++) {
                if (!tileAt(column, row).isWalkable()) { return false; }
            }
        }
        return true;
    }

    public static WorldLayout fromRoute(String route) {
        List<String> lines = route.lines().toList();
        if (lines.isEmpty()) throw new IllegalArgumentException("A route needs tiles");
        int columns = lines.get(0).length();
        WorldLayout layout = new WorldLayout(columns * TILE_SIZE, lines.size() * TILE_SIZE);
        for (int line = 0; line < lines.size(); line++) {
            String text = lines.get(line);
            if (text.length() != columns) throw new IllegalArgumentException("Route rows have different lengths");
            int row = lines.size() - 1 - line;
            for (int column = 0; column < columns; column++) {
                char symbol = text.charAt(column);
                layout.tiles[row][column] = switch (symbol) {
                    case '.' -> Tile.GRASS;
                    case '=' -> Tile.PATH;
                    case 'T' -> Tile.TREE;
                    case '~' -> Tile.WATER;
                    case '#' -> Tile.ROCK;
                    default -> {
                        boolean storyMarker = symbol >= 'a' && symbol <= 'z';
                        if (!storyMarker && "@>12B".indexOf(symbol) < 0) {
                            throw new IllegalArgumentException("Unknown route tile " + symbol);
                        }
                        if (layout.markers.put(symbol, new Cell(column, row)) != null)
                            throw new IllegalArgumentException("Duplicate route marker " + symbol);
                        yield Tile.PATH;
                    }
                };
            }
        }
        return layout;
    }

    public Cell marker(char symbol) {
        Cell cell = markers.get(symbol);
        if (cell == null) throw new IllegalArgumentException("Missing route marker " + symbol);
        return cell;
    }

    public static WorldLayout forWorld(String id, float width, float height,
                                       float spawnX, float spawnY, float portalX, float portalY) {
        WorldLayout layout = new WorldLayout(width, height);
        int variation = Math.floorMod(id == null ? 0 : id.hashCode(), 3);
        for (int row = 0; row < layout.rows(); row++) {
            for (int column = 0; column < layout.columns(); column++) {
                layout.tiles[row][column] = width == 1120 && height == 600 ? terrain(column, row, variation) : Tile.GRASS;
            }
        }
        int startX = (int) (spawnX / TILE_SIZE), startY = (int) (spawnY / TILE_SIZE);
        int endX = (int) (portalX / TILE_SIZE), endY = (int) (portalY / TILE_SIZE);
        layout.path(Math.min(startX, endX) - 1, startY - 1, Math.max(startX, endX) + 2, startY + 2);
        layout.path(endX - 1, Math.min(startY, endY) - 1, endX + 2, Math.max(startY, endY) + 2);
        return layout;
    }

    private void path(int left, int bottom, int right, int top) {
        for (int row = Math.max(0, bottom); row <= Math.min(rows() - 1, top); row++) {
            for (int column = Math.max(0, left); column <= Math.min(columns() - 1, right); column++) {
                tiles[row][column] = Tile.PATH;
            }
        }
    }

    private static Tile terrain(int x, int y, int variation) {
        if (x == 0 || x == 27 || y == 0 || y == 14) { return Tile.TREE; }
        if (y >= 6 && y <= 8) { return Tile.PATH; }
        if ((y >= 10 && y <= 12 && x >= 5 + variation && x <= 8 + variation)
                || (y >= 2 && y <= 3 && x >= 11 + variation && x <= 14 + variation)) { return Tile.WATER; }
        if ((x >= 18 && x <= 20 && y >= 10 && y <= 12)
                || (x >= 3 && x <= 4 && y >= 2 && y <= 3)) { return Tile.TREE; }
        if ((x == 22 + variation && y >= 3 && y <= 4)
                || (y == 11 && x >= 13 + variation && x <= 14 + variation)) { return Tile.ROCK; }
        return Tile.GRASS;
    }
}
