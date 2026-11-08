package com.thanos.participant1.patterns.facade;

import com.thanos.participant1.characters.Thanos;
import com.thanos.participant1.levels.Encounter;
import com.thanos.participant1.levels.World;
import com.thanos.participant1.levels.WorldCatalog;
import com.thanos.participant1.levels.WorldLayout;
import com.thanos.participant1.levels.WorldLayout.Cell;
import com.thanos.participant1.patterns.builder.WorldBuilder;
import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.battle.TurnBattle;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import static com.thanos.participant1.levels.WorldLayout.TILE_SIZE;
import static org.junit.jupiter.api.Assertions.*;

class CampaignTest {
    @Test
    void sixWorldsCanBeCompletedByWalkingAndUsingEarnedAttacks() {
        GameFacade game = new GameFacade(WorldCatalog.createCampaign(), new Random(4));
        game.newGame();
        for (int world = 0; world < 6; world++) {
            assertFalse(game.exitUnlocked());
            for (Encounter opponent : game.world().encounters()) {
                if (game.isCleared(opponent)) continue;
                walkTo(game, cell -> opponent.sees(game.world().layout(), cell.column(), cell.row()));
                assertTrue(game.isCleared(opponent), opponent.kind().displayName());
            }
            assertTrue(game.exitUnlocked());
            assertEquals(world + 1, game.inventory().stoneCount());
            Cell portal = game.world().layout().marker('>');
            walkTo(game, portal::equals);
            assertEquals(world == 5 ? TransitionResult.COMPLETED : TransitionResult.ADVANCED,
                    game.interactWithPortal());
        }
        assertTrue(game.inventory().isComplete());
        assertEquals(GamePhase.COMPLETE, game.phase());
        assertFalse(game.canResume());
        game.newGame();
        assertEquals(0, game.inventory().stoneCount());
        assertEquals("Titan", game.world().name());
    }

    @Test
    void fleeingRequiresLeavingSightBeforeTheEnemyCanSpotThanosAgain() {
        GameFacade game = trainingRoute();
        game.update(2, 1, 0);
        assertEquals(GamePhase.ALERT, game.phase());
        var stopped = game.player();
        game.pauseGame();
        game.showMenu();
        game.resumeGame();
        assertEquals(GamePhase.ALERT, game.phase());
        game.update(1, 1, 0);
        assertEquals(stopped, game.player());
        TurnBattle first = game.battle();
        game.pauseGame();
        game.resumeGame();
        assertSame(first, game.battle());
        first.flee();
        game.finishBattle();
        game.update(.2f, 0, 0);
        assertEquals(GamePhase.PLAYING, game.phase());
        game.update(TILE_SIZE / Thanos.SPEED, 1, 0);
        game.update(TILE_SIZE / Thanos.SPEED, -1, 0);
        assertEquals(GamePhase.ALERT, game.phase());
        assertEquals(Encounter.Kind.OUTRIDER, game.activeEncounter().kind());
    }

    @Test
    void aLostBossBattleReturnsToTheEntrancePreservingPriorRewards() {
        GameFacade game = trainingRoute();
        game.update(2, 1, 0);
        Encounter regular = game.activeEncounter();
        win(game);
        int xp = game.progress().experience();
        assertTrue(xp > 0);
        game.update(2 * TILE_SIZE / Thanos.SPEED, 1, 0);
        assertEquals(TransitionResult.LOCKED, game.interactWithPortal());
        game.update(1, 0, 0);
        TurnBattle boss = game.battle();
        assertFalse(boss.canFlee());
        for (int i = 0; i < 200 && !boss.state().isOver(); i++) {
            boss.playerDefend();
            boss.enemyTurn();
        }
        assertEquals(BattleState.LOST, boss.state());
        game.finishBattle();
        assertEquals(game.world().spawnX(), game.player().x());
        assertEquals(game.world().spawnY(), game.player().y());
        assertEquals(game.progress().maxHealth(), game.progress().health());
        assertEquals(game.inventory().maxEnergy(), game.inventory().energy());
        assertEquals(xp, game.progress().experience());
        assertTrue(game.isCleared(regular));
        assertFalse(game.exitUnlocked());
    }

    @Test
    void trainersBlockTheirTileUntilDefeated() {
        World route = new WorldBuilder().route("""
                TTTTTTTTT
                T.......T
                T@..1.>.T
                T.......T
                TTTTTTTTT
                """).encounter('1', Encounter.Kind.OUTRIDER, Encounter.Facing.EAST).build();
        GameFacade game = new GameFacade(List.of(route));
        game.newGame();
        Encounter trainer = route.encounters().get(0);
        game.update(2, 1, 0);
        assertEquals(GamePhase.PLAYING, game.phase());
        assertTrue(game.player().x() + Thanos.WIDTH <= trainer.x());
        // Slide past its corner, then step into its forward sight line.
        game.update(.25f, 1, 1);
        assertTrue(game.player().y() > trainer.y() + TILE_SIZE);
        walkTo(game, cell -> trainer.sees(route.layout(), cell.column(), cell.row()));
        assertTrue(game.isCleared(trainer));
        walkTo(game, new Cell(trainer.column(), trainer.row())::equals);
        assertEquals(trainer.x() + (TILE_SIZE - Thanos.WIDTH) / 2, game.player().x(), .01f);
    }

    private static GameFacade trainingRoute() {
        World route = new WorldBuilder().route("""
                TTTTTTTTTT
                T........T
                T....1..BT
                T@.....>.T
                T........T
                TTTTTTTTTT
                """).encounter('1', Encounter.Kind.OUTRIDER, Encounter.Facing.SOUTH)
                .encounter('B', Encounter.Kind.IN_BETWEENER, Encounter.Facing.WEST).build();
        GameFacade game = new GameFacade(List.of(route), new Random(1));
        game.newGame();
        return game;
    }

    private static void win(GameFacade game) {
        game.update(1, 0, 0);
        TurnBattle battle = game.battle();
        for (int i = 0; i < 100 && !battle.state().isOver(); i++) {
            if (battle.state() == BattleState.PLAYER_TURN) {
                Attack punch = game.inventory().empower(new BasicAttack("Punch", game.progress().punchDamage()));
                String preferred = game.progress().health() < game.progress().maxHealth() * .65 ? "Soul Mend" : "Planet Breaker";
                Attack chosen = game.inventory().abilities().stream().filter(ability -> ability.name().equals(preferred))
                        .findFirst().flatMap(ability -> game.inventory().use(ability, punch)).orElse(punch);
                battle.playerAttack(chosen);
            } else {
                battle.enemyTurn();
                if (battle.state() == BattleState.PLAYER_TURN) game.inventory().restoreEnergy(game.inventory().energyPerTurn());
            }
        }
        assertEquals(BattleState.WON, battle.state(), battle.enemy().name());
        game.finishBattle();
        int xp = game.progress().experience();
        int stones = game.inventory().stoneCount();
        game.finishBattle();
        assertEquals(xp, game.progress().experience());
        assertEquals(stones, game.inventory().stoneCount());
    }

    private static void walkTo(GameFacade game, Predicate<Cell> target) {
        Cell start = new Cell((int) ((game.player().x() + Thanos.WIDTH / 2) / TILE_SIZE),
                (int) ((game.player().y() + Thanos.FEET_HEIGHT / 2) / TILE_SIZE));
        WorldLayout layout = game.world().layout();
        var frontier = new ArrayDeque<Cell>();
        Map<Cell, Cell> parent = new HashMap<>();
        frontier.add(start);
        parent.put(start, null);
        while (!frontier.isEmpty()) {
            Cell cell = frontier.remove();
            if (target.test(cell)) {
                List<Cell> path = new ArrayList<>();
                for (Cell step = cell; step != null; step = parent.get(step)) path.add(step);
                Collections.reverse(path);
                for (Cell step : path) walkToCenter(game, step);
                return;
            }
            for (Encounter.Facing direction : Encounter.Facing.values()) {
                Cell next = new Cell(cell.column() + direction.dx(), cell.row() + direction.dy());
                if (!parent.containsKey(next) && next.column() >= 0 && next.column() < layout.columns()
                        && next.row() >= 0 && next.row() < layout.rows()
                        && game.canStandAt((next.column() + .5f) * TILE_SIZE - Thanos.WIDTH / 2,
                                (next.row() + .5f) * TILE_SIZE - Thanos.FEET_HEIGHT / 2)) {
                    parent.put(next, cell);
                    frontier.add(next);
                }
            }
        }
        fail("Unreachable destination in " + game.world().name());
    }

    private static void walkToCenter(GameFacade game, Cell cell) {
        float x = (cell.column() + .5f) * TILE_SIZE - Thanos.WIDTH / 2;
        float y = (cell.row() + .5f) * TILE_SIZE - Thanos.FEET_HEIGHT / 2;
        for (int attempt = 0; attempt < 6; attempt++) {
            if (game.phase() == GamePhase.ALERT) win(game);
            float dx = x - game.player().x(), dy = y - game.player().y();
            if (Math.abs(dx) + Math.abs(dy) < .01f) return;
            game.update(Math.abs(dx == 0 ? dy : dx) / Thanos.SPEED,
                    Math.signum(dx), dx == 0 ? Math.signum(dy) : 0);
        }
        fail("Blocked walking to " + cell + " in " + game.world().name());
    }
}
