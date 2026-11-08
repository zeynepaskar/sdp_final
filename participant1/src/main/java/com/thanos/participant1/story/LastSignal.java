package com.thanos.participant1.story;

import com.thanos.participant1.levels.World;
import com.thanos.participant1.levels.WorldLayout.Tile;
import com.thanos.participant1.patterns.builder.WorldBuilder;

import java.util.Set;

import static com.thanos.participant1.story.Dialogue.say;
import static com.thanos.participant1.story.StoryCommand.inspect;
import static com.thanos.participant1.story.StoryCommand.returnToRoute;
import static com.thanos.participant1.story.StoryCommand.talk;

/** The two optional stops on the evacuation recording's journey. */
public final class LastSignal {
    private static final String ORIGINAL = "signal.original";
    private static final String BACKUP = "signal.backup";
    private static final String BROADCAST = "signal.broadcast";
    private static final String PRIVATE = "signal.private";

    private LastSignal() { }

    public static World observatory() {
        return new WorldBuilder().id("observatory").name("Titan").subtitle("Ruined Observatory")
                .colors(0xB8B9C9FF, 0x878B9FFF).route("""
                        ############################
                        #..........................#
                        #..T..................T....#
                        #.T......###########.......#
                        #......###....=....###.....#
                        #.....##....==r==....##....#
                        #....##..g....=.......##...#
                        #.>@===================#...#
                        #....##.......=.......##...#
                        #.....##......=......##....#
                        #......###..~~~~~..###.....#
                        #........###########.....T.#
                        #..T....................T..#
                        #..........................#
                        ############################
                        """, Tile.STONEWORK)
                .marker('>', "Caldera Rim", "return-door", returnToRoute())
                .marker('g', "Gamora", "gamora", talk(LastSignal::gamoraOnTitan))
                .marker('r', "Receiver", "receiver", inspect(LastSignal::originalRecording))
                .build();
    }

    public static World dock() {
        return new WorldBuilder().id("salvage-dock").name("Knowhere").subtitle("Salvage Dock")
                .colors(0xCAC0AEFF, 0x75969BFF).route("""
                        ############################
                        #===============#==========#
                        #=======#=#==#=============#
                        #=#=================g====#=#
                        #==========================#
                        #=======n=~~~====~~~=====~~#
                        #=========~~~====~~~==r==~~#
                        #=>@=====================~~#
                        #========================~~#
                        #=========~~~==b=~~~===#=~~#
                        #~~~~~~~~~~~~=#==~~~=====~~#
                        #~~~~~~~~~~~~====~~~~~~~~~~#
                        #~~~~~~~~~~~~~~~~~~~~~~~~~~#
                        #~~~~~~~~~~~~~~~~~~~~~~~~~~#
                        ############################
                        """, Tile.BOARDWALK)
                .marker('>', "Skull Station", "return-door", returnToRoute())
                .marker('n', "Nebula", "nebula", talk(LastSignal::nebula))
                .marker('b', "Backup spool", "backup", inspect(LastSignal::backupRecording))
                .marker('r', "Dock radio", "radio", inspect(LastSignal::dockRadio))
                .marker('g', "Gamora", "gamora", talk(LastSignal::gamoraOnDock))
                .build();
    }

    private static Dialogue gamoraOnTitan(Set<String> flags) {
        if (flags.contains(ORIGINAL)) {
            return say("Gamora", "That's Lio. I'd know that voice through three walls and a cheap speaker.",
                    "Nebula found a transmitter at the salvage dock on Knowhere. Take it to her.");
        }
        return say("Gamora", "Hear that click? Lio always tapped the mic twice. Once to test it. Once to annoy the tower.",
                "His shuttle went back during the evacuation. Check the dish. Maybe it caught his last call.");
    }

    private static Dialogue originalRecording(Set<String> flags) {
        if (flags.contains(ORIGINAL)) return say("Receiver", "The empty reel ticks twice, then stops.");
        return say("Receiver", "Two taps. Then a pilot's voice.",
                "Tower, this is Seven. One lamp still lit on the ridge. I'm going back.",
                "Kid under seat four, blink our lamp twice. Let them know we see them.\n\nYou take the original spool.")
                .remember(ORIGINAL);
    }

    private static Dialogue backupRecording(Set<String> flags) {
        if (flags.contains(ORIGINAL)) return say("Backup spool", "The same two taps. Your original is clearer.");
        if (flags.contains(BACKUP)) return say("Backup spool", "Only the paper sleeve is left. Someone wrote SEVEN on it twice.");
        return say("Backup spool", "A shipping label holds a cracked reel together. It plays in bursts.",
                "...one lamp... going back... kid under seat four...\n\nYou salvage the damaged copy.")
                .remember(BACKUP);
    }

    private static Dialogue nebula(Set<String> flags) {
        if (flags.contains(BROADCAST)) return say("Nebula", "Fifteen ships asked for the pilot's name. I sent it fifteen times.");
        if (flags.contains(PRIVATE)) return say("Nebula", "I put the transmitter away. She's waiting by the far berth.");
        if (!flags.contains(ORIGINAL) && !flags.contains(BACKUP)) {
            return say("Nebula", "The dock charged me storage on a distress call. Apparently silence takes up shelf space.",
                    "Try the backup spool on the middle pier. It came off Shuttle Seven.");
        }
        String opening = flags.contains(ORIGINAL)
                ? "A clean spool. Gamora owes me three credits. She had you down for 'used it as a coaster.'"
                : "The copy drops every third word. I can still hear him turn the ship around.";
        return say("Nebula", opening,
                "The manifest says nobody went back. This will correct it. Unless you'd rather Gamora heard it alone.")
                .choose(new Dialogue.Option("Broadcast the call", BROADCAST,
                                say("Nebula", "Channel open. Listen to the dock radio. Somebody's answering.")),
                        new Dialogue.Option("Give it to Gamora", PRIVATE,
                                say("Nebula", "I'll take it to her. Come by the far berth when you're ready.")));
    }

    private static Dialogue dockRadio(Set<String> flags) {
        if (flags.contains(BROADCAST)) {
            String transmission = flags.contains(ORIGINAL)
                    ? "Two taps. Lio's voice carries across the docks: One lamp still lit on the ridge. I'm going back."
                    : "Through the static, every speaker carries the same words: One lamp... going back...";
            return say("Dock radio", transmission, "A tug breaks radio silence. Seven, we see your light.");
        }
        return say("Dock radio", "Berth three, please weigh your cargo again. 'Emotionally heavy' is not a unit.");
    }

    private static Dialogue gamoraOnDock(Set<String> flags) {
        if (flags.contains(PRIVATE)) {
            return say("Gamora", "Seat four was mine. I thought I'd forgotten his voice.",
                    "I kept the lamp. Changed the battery yesterday. Funny thing to still pack.",
                    "Give me a minute with the spool. Thanks.");
        }
        if (flags.contains(BROADCAST)) return say("Gamora", "See the tug flashing its running lights? That's how pilots say thank you.");
        return say("Gamora", "Nebula's on the short pier. You can spot her by the queue of officials avoiding her.");
    }
}
