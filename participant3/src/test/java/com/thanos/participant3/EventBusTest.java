package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventBusTest {
    @Test
    void everySubscriberGetsTheDamageEvent() {
        EventBus bus = new EventBus();
        List<GameEvent> hud = new ArrayList<>(), log = new ArrayList<>();
        bus.subscribe(hud::add);
        bus.subscribe(log::add);

        new HitResolver(bus).resolve(new FakeCombatant("Thanos", 1000, 1000, 0),
                new FakeCombatant("Iron Man", 100, 100, 15), new BasicAttack("Punch", 50));

        assertEquals(hud, log);
        assertEquals(new GameEvent(GameEventType.DAMAGE, "Iron Man", "Iron Man received 35 damage", 35), hud.get(0));
        assertEquals("Iron Man health: 65/100", hud.get(1).message());
    }

    @Test
    void battleLogicKeepsWorkingWithoutSubscribers() {
        EventBus bus = new EventBus();
        GameEventListener hud = event -> { };
        bus.subscribe(hud);
        bus.unsubscribe(hud);
        FakeCombatant target = new FakeCombatant("Thor", 100, 100, 0);

        int dealt = new HitResolver(bus).resolve(new FakeCombatant("Thanos", 1000, 1000, 0), target,
                new BasicAttack("Punch", 40));

        assertEquals(0, bus.listenerCount());
        assertEquals(40, dealt);
        assertEquals(60, target.health());
    }

    @Test
    void subscribingTwiceDeliversOnce() {
        EventBus bus = new EventBus();
        List<GameEvent> received = new ArrayList<>();
        GameEventListener listener = received::add;
        bus.subscribe(listener);
        bus.subscribe(listener);

        bus.publish(GameEventType.HEAL, "Thanos", "Thanos restored 10 health", 10);

        assertEquals(1, received.size());
    }

    @Test
    void listenerMayUnsubscribeWhileBeingNotified() {
        EventBus bus = new EventBus();
        List<GameEvent> received = new ArrayList<>();
        GameEventListener once = new GameEventListener() {
            public void onEvent(GameEvent event) {
                received.add(event);
                bus.unsubscribe(this);
            }
        };
        bus.subscribe(once);

        bus.publish(GameEventType.HEAL, "Thanos", "first", 1);
        bus.publish(GameEventType.HEAL, "Thanos", "second", 1);

        assertEquals(1, received.size());
    }

    @Test
    void defeatIsAnnounced() {
        EventBus bus = new EventBus();
        List<GameEvent> received = new ArrayList<>();
        bus.subscribe(received::add);

        new HitResolver(bus).resolve(new FakeCombatant("Thanos", 1000, 1000, 0),
                new FakeCombatant("Captain America", 20, 100, 0), new BasicAttack("Punch", 50));

        assertEquals(GameEventType.DEFEATED, received.get(received.size() - 1).type());
    }
}
