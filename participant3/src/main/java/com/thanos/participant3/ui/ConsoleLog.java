package com.thanos.participant3.ui;

import com.thanos.participant3.patterns.observer.GameEvent;
import com.thanos.participant3.patterns.observer.GameEventListener;

/** Second observer: writes every event to the console, independent of the HUD. */
public final class ConsoleLog implements GameEventListener {
    public void onEvent(GameEvent event) {
        System.out.println("[" + event.type() + "] " + event.message());
    }
}
