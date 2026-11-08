package com.thanos.participant1.story;

import java.util.List;
import java.util.Objects;

/** Immutable content. A line or choice can record a fact for later conversations. */
public record Dialogue(String speaker, List<String> pages, List<Option> options, String completionFlag) {
    public record Option(String label, String flag, Dialogue reply) {
        public Option {
            if (label.isBlank() || flag.isBlank()) throw new IllegalArgumentException("Choice needs a label and a flag");
            Objects.requireNonNull(reply);
        }
    }

    public Dialogue {
        pages = List.copyOf(pages);
        options = List.copyOf(options);
        if (speaker.isBlank() || pages.isEmpty() || pages.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("Dialogue needs a speaker and at least one line");
        }
    }

    public static Dialogue say(String speaker, String... pages) {
        return new Dialogue(speaker, List.of(pages), List.of(), null);
    }

    public Dialogue remember(String flag) {
        if (flag.isBlank()) throw new IllegalArgumentException("Story flag needs a name");
        return new Dialogue(speaker, pages, options, flag);
    }

    public Dialogue choose(Option... options) {
        if (options.length < 2) throw new IllegalArgumentException("Offer at least two choices");
        return new Dialogue(speaker, pages, List.of(options), completionFlag);
    }
}
