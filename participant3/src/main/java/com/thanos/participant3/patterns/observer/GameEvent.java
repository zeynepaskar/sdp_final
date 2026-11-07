package com.thanos.participant3.patterns.observer;

/** Immutable event passed through the EventBus. */
public record GameEvent(GameEventType type, String source, String message, int value) {
    public GameEvent {
        if (type == null || source == null || source.isBlank() || message == null || message.isBlank()) {
            throw new IllegalArgumentException("Event needs a type, source and message");
        }
    }
}
