package com.thanos.participant1.levels;

import com.thanos.participant1.patterns.builder.WorldBuilder;
import com.thanos.participant1.story.LastSignal;
import com.thanos.participant1.story.StoryCommand;
import java.util.List;

import static com.thanos.participant1.levels.Encounter.Facing.*;
import static com.thanos.participant1.levels.Encounter.Kind.*;

public final class WorldCatalog {
    private WorldCatalog() { }

    public static List<World> createCampaign() {
        return List.of(titan(), xandar(), knowhere(), vormir(), asgard(), earth());
    }

    private static World titan() {
        return new WorldBuilder().id("titan").name("Titan").subtitle("Caldera Rim")
                .colors(0xC6CBA3FF, 0x8D8C9CFF).route("""
                        ############################
                        #..........................#
                        #....................##....#
                        #..#.================......#
                        #.##.=..1########...=......#
                        #..d.=..###~~~~###..=......#
                        #....=..##~~~~~~##..=......#
                        #.@===..##~~~~~~##..===B=>.#
                        #....=..##~~~~~~##..=......#
                        #....=..###~~~~###..=......#
                        #....=...#######2...=......#
                        #....================.##...#
                        #...............#..........#
                        #..........................#
                        ############################
                        """)
                .encounter('1', OUTRIDER, NORTH)
                .encounter('2', CHITAURI, SOUTH)
                .marker('d', "Ruined Observatory", "observatory-door", StoryCommand.travel(LastSignal.observatory()))
                .encounter('B', IN_BETWEENER, WEST)
                .build();
    }

    private static World xandar() {
        return new WorldBuilder().id("xandar").name("Xandar").subtitle("Twin Canals")
                .colors(0xB3D3C1FF, 0x659687FF).route("""
                        TTTTTTTTTTTTTTTTTTTTTTTTTTTT
                        T......~~~~~....~~~........T
                        T.TTT..~~~~~....~~~.#......T
                        T.TTT..~~~~~....~~~........T
                        T.TTT..~~~~~.==========B=>.T
                        T...==========..~~~2.=.....T
                        T...=..~~~..==========.....T
                        T...=.1~~~...=..~~~........T
                        T...=..~~~..#=..~~~........T
                        T...==========..~~~...TTT..T
                        T...=..~~~......~~~~~.TTT..T
                        T.@==..~~~......~~~~~.TTT..T
                        T......~~~......~~~~~......T
                        T......~~~......~~~~~......T
                        TTTTTTTTTTTTTTTTTTTTTTTTTTTT
                        """)
                .encounter('1', CHITAURI, SOUTH)
                .encounter('2', SAKAARAN, NORTH)
                .encounter('B', CHAMPION, WEST)
                .build();
    }

    private static World knowhere() {
        return new WorldBuilder().id("knowhere").name("Knowhere").subtitle("Skull Station")
                .colors(0xD5B69CFF, 0xA07475FF).route("""
                        ############################
                        #..........................#
                        #...d...###########........#
                        #......#############..##...#
                        #.@==..##~~~###~~~##..#....#
                        #...=..##~~~###~~~##.......#
                        #...=..##~~~###~~~##.......#
                        #...=..######~######.......#
                        #...==================.....#
                        #....=.###1#########==.....#
                        #.##.=..##.#.#.#.##.==.....#
                        #....=....#######2..===B=>.#
                        #....================......#
                        #..........................#
                        ############################
                        """)
                .encounter('1', SAKAARAN, NORTH)
                .encounter('2', OUTRIDER, SOUTH)
                .marker('d', "Salvage Dock", "dock-door", StoryCommand.travel(LastSignal.dock()))
                .encounter('B', GARDENER, WEST)
                .build();
    }

    private static World vormir() {
        return new WorldBuilder().id("vormir").name("Vormir").subtitle("Cliff Stair")
                .colors(0xBCB6CAFF, 0x80738FFF).route("""
                        ############################
                        #..............######......#
                        #..............######......#
                        #.........###======........#
                        #.........###=##..=====B=>.#
                        #....####=====...2=........#
                        #....####=#...=====....##..#
                        #######..=....=......##....#
                        #######..======............#
                        #.....=====................#
                        #.....=====..~~~~~~~~......#
                        #.....=.1....~~~~~~~~~~~...#
                        #.@====.........~~~~~~~~...#
                        #..........................#
                        ############################
                        """)
                .encounter('1', OUTRIDER, NORTH)
                .encounter('2', SAKAARAN, SOUTH)
                .encounter('B', RUNNER, WEST)
                .build();
    }

    private static World asgard() {
        return new WorldBuilder().id("asgard").name("Asgard").subtitle("Bifrost Causeway")
                .colors(0xCED1A0FF, 0x849762FF).route("""
                        TTTTTTTTTTTTTTTTTTTTTTTTTTTT
                        T....~~~~~~~~~~~~~~~.......T
                        T....~~~~~~~~~~~~~~~.......T
                        T...=========~~======......T
                        T...=~~~~~..====~2~~=#.#.#.T
                        T...=~~~~~.TTT.~~~~~=......T
                        T...=~~~~~.TTT.~~~~~=......T
                        T.@====================B=>.T
                        T....~~~~~.TTT.~~~~~.......T
                        T....~~~~~1TTT.~~~~~.......T
                        T....~~~~~.....~~~~~.#.#.#.T
                        T....~~~~~~~~~~~~~~~.......T
                        T....~~~~~~~~~~~~~~~.......T
                        T....~~~~~~~~~~~~~~~.......T
                        TTTTTTTTTTTTTTTTTTTTTTTTTTTT
                        """)
                .encounter('1', CHITAURI, NORTH)
                .encounter('2', OUTRIDER, NORTH)
                .encounter('B', COLLECTOR, WEST)
                .build();
    }

    private static World earth() {
        return new WorldBuilder().id("earth").name("Earth").subtitle("Woodland Lakes")
                .colors(0xBED3A1FF, 0x699763FF).route("""
                        TTTTTTTTTTTTTTTTTTTTTTTTTTTT
                        TTTTTTTTTTTTTTTTTTTTTTTTTTTT
                        T.======.TTTTTTTT..........T
                        T.@.~~.=.TTTTTTTT..........T
                        T..~~~~=......TTT.....##...T
                        T..~~~~=..=========.....#..T
                        T..~~~~=1.=...~~.2=........T
                        T...~~.=..=..~~~~.=........T
                        TTTTTT.====..~~~~.=====B=>.T
                        TTTTTT....=..~~~~.=........T
                        TTTTTT....=..~~~~.=........T
                        TTTTTTTTTT===.~~..=........T
                        TTTTTTTTTT..=======..TTTTTTT
                        TTTTTTTTTTTTTTTTTTTTTTTTTTTT
                        TTTTTTTTTTTTTTTTTTTTTTTTTTTT
                        """)
                .encounter('1', SAKAARAN, WEST)
                .encounter('2', CHITAURI, NORTH)
                .encounter('B', GRANDMASTER, WEST)
                .build();
    }
}
