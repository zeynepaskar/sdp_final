package com.thanos.participant1.story;

import com.thanos.participant1.levels.World;
import com.thanos.participant1.levels.WorldCatalog;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastSignalTest {
    private final StorySession story = new StorySession(new StorySession.Host() {
        public void visit(World area) { location = area; }
        public void returnToRoute() { throw new AssertionError("Unexpected return"); }
        public void interactNearby() { throw new AssertionError("No dialogue open"); }
        public void pauseGame() { throw new AssertionError("No dialogue open"); }
    });
    private World location;

    @Test
    void broadcastPlaysTheOriginalWhenTheObservatoryWasVisited() {
        visit("titan", "observatory");
        interact('r');
        finishReading();
        assertTrue(story.hasFlag("signal.original"));

        visit("knowhere", "salvage-dock");
        interact('n');
        assertTrue(story.view().text().contains("clean spool"));
        story.confirm();
        assertEquals(List.of("Broadcast the call", "Give it to Gamora"), story.view().choices());
        story.confirm();
        finishReading();
        interact('r');
        assertTrue(finishReading().stream().anyMatch(line -> line.contains("we see your light")));
        interact('n');
        assertTrue(story.view().choices().isEmpty());
        assertTrue(story.hasFlag("signal.broadcast"));
        assertFalse(story.hasFlag("signal.private"));
    }

    @Test
    void theBackupCanBeGivenPrivatelyAfterSkippingTitan() {
        visit("knowhere", "salvage-dock");
        interact('n');
        assertTrue(finishReading().stream().anyMatch(line -> line.contains("middle pier")));
        interact('b');
        finishReading();
        assertTrue(story.hasFlag("signal.backup"));
        assertFalse(story.hasFlag("signal.original"));

        interact('n');
        assertTrue(story.view().text().contains("third word"));
        story.confirm();
        story.navigate(1);
        story.cancel();
        assertFalse(story.hasFlag("signal.private"));
        assertFalse(story.hasFlag("signal.broadcast"));

        interact('n');
        story.confirm();
        story.navigate(1);
        story.confirm();
        finishReading();
        interact('g');
        assertTrue(finishReading().stream().anyMatch(line -> line.contains("Seat four was mine")));
        interact('r');
        assertTrue(finishReading().stream().noneMatch(line -> line.contains("Lio")));
        assertTrue(story.hasFlag("signal.private"));
        assertFalse(story.hasFlag("signal.broadcast"));
    }

    private void visit(String route, String destination) {
        location = WorldCatalog.createCampaign().stream().filter(w -> route.equals(w.id())).findFirst().orElseThrow();
        interact('d');
        assertEquals(destination, location.id());
    }

    private void interact(char symbol) {
        var cell = location.layout().marker(symbol);
        location.markers().stream().filter(marker -> marker.cell().equals(cell)).findFirst().orElseThrow()
                .command().execute(story);
    }

    private List<String> finishReading() {
        List<String> pages = new ArrayList<>();
        while (story.view() != null) {
            assertTrue(story.view().choices().isEmpty(), "Choose an ending explicitly");
            pages.add(story.view().text());
            story.confirm();
        }
        return pages;
    }
}
