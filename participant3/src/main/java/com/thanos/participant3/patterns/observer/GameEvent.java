package com.thanos.participant3.patterns.observer;

/** Something that happened in the game; source is who it happened to, value is the number involved. */
public record GameEvent(GameEventType type, String source, String message, int value) {
    public GameEvent {
        if (type == null || source == null || source.isBlank() || message == null || message.isBlank()) {
            throw new IllegalArgumentException("Event needs a type, source and message");
        }
    }
}
