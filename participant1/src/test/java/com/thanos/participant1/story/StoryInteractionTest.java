package com.thanos.participant1.story;

import com.thanos.participant1.levels.World;
import com.thanos.participant1.patterns.builder.WorldBuilder;
import com.thanos.participant1.patterns.facade.GameFacade;
import com.thanos.participant1.patterns.facade.GamePhase;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StoryInteractionTest {
    @Test
    void cancellingLeavesFlagsUntouchedAndConfirmationChoosesOnlyOnce() {
        Dialogue question = Dialogue.say("Courier", "Still sealed.", "Open it?")
                .choose(new Dialogue.Option("Open", "opened", Dialogue.say("Courier", "There it is.")),
                        new Dialogue.Option("Keep", "kept", Dialogue.say("Courier", "Safe with me.")));
        World world = new WorldBuilder().route("""
                ......
                .@n.>.
                ......
                """).marker('n', "Courier", "courier", StoryCommand.talk(flags -> question)).build();
        GameFacade game = new GameFacade(List.of(world));
        game.newGame();
        game.confirm();
        assertEquals("Still sealed.", game.dialogue().text());
        var position = game.player();
        game.update(1, 0, 1);
        assertEquals(position, game.player());

        game.confirm();
        assertEquals(List.of("Open", "Keep"), game.dialogue().choices());
        game.navigateStory(1);
        assertEquals(1, game.dialogue().selected());
        game.cancel();
        assertNull(game.dialogue());
        assertFalse(game.hasStoryFlag("kept"));

        game.confirm();
        game.confirm();
        game.navigateStory(-1);
        game.confirm();
        assertTrue(game.hasStoryFlag("kept"));
        assertFalse(game.hasStoryFlag("opened"));
        assertEquals("Safe with me.", game.dialogue().text());
        game.confirm();
        assertNull(game.dialogue());
        game.newGame();
        assertFalse(game.hasStoryFlag("kept"));
    }

    @Test
    void doorRoundTripReturnsToSameSpotAndPreservesReadFacts() {
        World room = new WorldBuilder().id("annex").route("""
                ......
                .@r...
                .>....
                ......
                """)
                .marker('r', "Receiver", "receiver", StoryCommand.inspect(flags -> Dialogue.say("Tape", "A signal.").remember("heard")))
                .marker('>', "Return", "door", StoryCommand.returnToRoute()).build();
        World route = new WorldBuilder().route("""
                ......
                .@d.>.
                ......
                """).marker('d', "Annex", "door", StoryCommand.travel(room)).build();
        GameFacade game = new GameFacade(List.of(route));
        game.newGame();
        var departure = game.player();
        game.confirm();
        assertTrue(game.inSideArea());
        assertSame(room, game.world());
        game.confirm();
        game.cancel();
        assertFalse(game.hasStoryFlag("heard"));
        game.confirm();
        game.pauseGame();
        game.resumeGame();
        assertEquals("A signal.", game.dialogue().text());
        game.confirm();
        assertTrue(game.hasStoryFlag("heard"));

        // Walk round the south side of the return door, away from the receiver.
        game.update(1 / 6f, -1, 0);
        game.update(1 / 6f, 0, -1);
        assertEquals("Return", game.nearbyMarker().name());
        game.confirm();
        assertSame(route, game.world());
        assertEquals(departure, game.player());
        assertTrue(game.hasStoryFlag("heard"));
        assertEquals(GamePhase.PLAYING, game.phase());
    }
}
