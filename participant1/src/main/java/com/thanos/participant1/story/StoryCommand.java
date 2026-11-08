package com.thanos.participant1.story;

import com.thanos.participant1.levels.World;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/** A placed character, object, or doorway carries a command that runs on confirmation. */
@FunctionalInterface
public interface StoryCommand {
    void execute(StorySession session);

    static StoryCommand talk(Function<Set<String>, Dialogue> conversation) {
        return session -> session.open(conversation);
    }

    static StoryCommand inspect(Function<Set<String>, Dialogue> description) {
        return session -> session.open(description);
    }

    static StoryCommand travel(World area) {
        Objects.requireNonNull(area);
        return session -> session.visit(area);
    }

    static StoryCommand returnToRoute() {
        return StorySession::returnToRoute;
    }
}
