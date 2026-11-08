package com.thanos.participant1.story;

import com.thanos.participant1.levels.WorldLayout.Cell;
import java.util.Objects;

public record StoryMarker(Cell cell, String name, String sprite, StoryCommand command) {
    public StoryMarker {
        Objects.requireNonNull(cell);
        Objects.requireNonNull(command);
        if (name.isBlank() || sprite.isBlank()) throw new IllegalArgumentException("Marker needs a name and sprite");
    }
}
