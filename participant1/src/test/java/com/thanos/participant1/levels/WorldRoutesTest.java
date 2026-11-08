package com.thanos.participant1.levels;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static com.thanos.participant1.levels.Encounter.Facing.*;
import static com.thanos.participant1.levels.Encounter.Kind.OUTRIDER;
import static org.junit.jupiter.api.Assertions.*;

class WorldRoutesTest {
    @Test
    void guardsSeeUpToFourTilesInFrontAndNeverAroundAnObstacle() {
        WorldLayout clear = WorldLayout.fromRoute("""
                .......
                .......
                .......
                .......
                .......
                .......
                .......
                """);
        for (Encounter.Facing facing : Encounter.Facing.values()) {
            int x = facing == EAST ? 1 : facing == WEST ? 5 : 3;
            int y = facing == NORTH ? 1 : facing == SOUTH ? 5 : 3;
            Encounter guard = new Encounter(OUTRIDER, x, y, facing);
            int dx = facing.dx(), dy = facing.dy();
            assertTrue(guard.sees(clear, x + dx, y + dy));
            assertTrue(guard.sees(clear, x + 4 * dx, y + 4 * dy));
            assertFalse(guard.sees(clear, x + 5 * dx, y + 5 * dy));
            assertFalse(guard.sees(clear, x - dx, y - dy));
            assertFalse(guard.sees(clear, x + dx + dy, y + dy + dx));
        }

        Encounter guard = new Encounter(OUTRIDER, 1, 0, EAST);
        assertFalse(guard.sees(WorldLayout.fromRoute("..T..."), 4, 0));
        assertFalse(guard.sees(WorldLayout.fromRoute("...#.."), 4, 0));
        assertTrue(guard.sees(WorldLayout.fromRoute("..~~.."), 4, 0));
    }

    @Test
    void everyEncounterAndExitCanBeReachedAndRegularSightCanBeAvoided() {
        var campaign = WorldCatalog.createCampaign();
        assertEquals(6, campaign.size());
        Set<Encounter.Kind> bosses = new HashSet<>();
        for (World world : campaign) {
            assertEquals(2, world.encounters().stream().filter(e -> !e.isBoss()).count(), world.id());
            var boss = world.encounters().stream().filter(Encounter::isBoss).findFirst().orElseThrow();
            assertTrue(bosses.add(boss.kind()), world.id());
            Set<WorldLayout.Cell> accessible = flood(world, cell -> true);
            for (Encounter enemy : world.encounters()) {
                assertTrue(accessible.stream().anyMatch(c -> enemy.sees(world.layout(), c.column(), c.row())), world.id());
            }
            assertTrue(accessible.contains(world.layout().marker('>')), world.id());
            var avoidRegulars = flood(world, c -> world.encounters().stream().noneMatch(e ->
                    !e.isBoss() && e.sees(world.layout(), c.column(), c.row())));
            assertTrue(avoidRegulars.stream().anyMatch(c -> boss.sees(world.layout(), c.column(), c.row())), world.id());
        }
    }

    private Set<WorldLayout.Cell> flood(World world, Predicate<WorldLayout.Cell> allowed) {
        var layout = world.layout();
        var queue = new ArrayDeque<WorldLayout.Cell>();
        var visited = new HashSet<WorldLayout.Cell>();
        var start = layout.marker('@');
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            var current = queue.remove();
            for (var step : Encounter.Facing.values()) {
                int x = current.column() + step.dx(), y = current.row() + step.dy();
                if (x < 0 || y < 0 || x >= layout.columns() || y >= layout.rows()) continue;
                var next = new WorldLayout.Cell(x, y);
                boolean occupied = world.encounters().stream().anyMatch(e -> e.column() == x && e.row() == y);
                if (!occupied && layout.tileAt(x, y).isWalkable() && allowed.test(next) && visited.add(next))
                    queue.add(next);
            }
        }
        return visited;
    }
}
