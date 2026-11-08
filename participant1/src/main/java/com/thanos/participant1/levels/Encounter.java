package com.thanos.participant1.levels;

import java.util.Objects;

public record Encounter(Kind kind, int column, int row, Facing facing) {
    public enum Kind {
        OUTRIDER("Outrider", false), CHITAURI("Chitauri", false), SAKAARAN("Sakaaran", false),
        IN_BETWEENER("In-Betweener", true), CHAMPION("Champion", true), GARDENER("Gardener", true),
        RUNNER("Runner", true), COLLECTOR("Collector", true), GRANDMASTER("Grandmaster", true);

        private final String displayName;
        private final boolean boss;

        Kind(String displayName, boolean boss) {
            this.displayName = displayName;
            this.boss = boss;
        }

        public String displayName() { return displayName; }
    }

    public enum Facing {
        NORTH(0, 1), EAST(1, 0), SOUTH(0, -1), WEST(-1, 0);

        private final int dx;
        private final int dy;

        Facing(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public int dx() { return dx; }
        public int dy() { return dy; }
    }

    private static final int SIGHT_TILES = 4;

    public Encounter {
        Objects.requireNonNull(kind);
        Objects.requireNonNull(facing);
    }

    public boolean isBoss() { return kind.boss; }
    public float x() { return column * WorldLayout.TILE_SIZE; }
    public float y() { return row * WorldLayout.TILE_SIZE; }

    public boolean sees(WorldLayout layout, int playerColumn, int playerRow) {
        for (int distance = 1; distance <= SIGHT_TILES; distance++) {
            int x = column + distance * facing.dx();
            int y = row + distance * facing.dy();
            if (x < 0 || y < 0 || x >= layout.columns() || y >= layout.rows()) return false;
            WorldLayout.Tile tile = layout.tileAt(x, y);
            if (tile == WorldLayout.Tile.TREE || tile == WorldLayout.Tile.ROCK) return false;
            if (x == playerColumn && y == playerRow) return true;
        }
        return false;
    }
}
