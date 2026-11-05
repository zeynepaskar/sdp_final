package com.thanos.participant1.levels;

import com.thanos.participant1.patterns.builder.WorldBuilder;
import java.util.List;

/** Director: builds six worlds through the same construction steps. */
public final class WorldCatalog {
    private WorldCatalog() { }

    public static List<World> createCampaign() {
        return List.of(
                world("titan", "Titan", 0xC6CBA3FF, 0x8D8C9CFF),
                world("xandar", "Xandar", 0xB3D3C1FF, 0x659687FF),
                world("knowhere", "Knowhere", 0xD5B69CFF, 0xA07475FF),
                world("vormir", "Vormir", 0xBCB6CAFF, 0x80738FFF),
                world("asgard", "Asgard", 0xCED1A0FF, 0x849762FF),
                world("earth", "Earth", 0xBED3A1FF, 0x699763FF));
    }

    private static World world(String id, String name, int grass, int leaves) {
        return new WorldBuilder().id(id).name(name).colors(grass, leaves)
                .size(1120, 600).spawn(90, 270).portal(1000, 240, 56, 110).build();
    }
}
