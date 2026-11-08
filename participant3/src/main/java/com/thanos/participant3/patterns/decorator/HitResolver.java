package com.thanos.participant3.patterns.decorator;

import com.thanos.participant3.Combatant;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEventType;
import java.util.HashMap;
import java.util.Map;

/** Applies a decorated attack to a target and publishes the result; handles Portal and rewind. */
public final class HitResolver {
    private final EventBus events;
    private final Portal portal;
    private final Map<String, Integer> lastWound = new HashMap<>();

    public HitResolver(EventBus events) { this(events, Portal.NONE); }

    public HitResolver(EventBus events, Portal portal) {
        if (events == null || portal == null) {
            throw new IllegalArgumentException("Resolver needs an event bus and a portal");
        }
        this.events = events;
        this.portal = portal;
    }

    public int resolve(Combatant attacker, Combatant target, Attack attack) {
        if (attacker == null || target == null || attack == null) {
            throw new IllegalArgumentException("Attacker, target and attack are required");
        }
        int defense = Math.max(0, Math.min(80, target.defense()));
        int dealt = (attack.ignoresDefense() || attack.unblockable())
                ? attack.damage()
                : attack.damage() * (100 - defense) / 100;
        if (portal.opensFor(target)) {
            lastWound.put(target.name(), 0);
            sendBack(attacker, target, dealt);
            return 0;
        }
        target.takeDamage(dealt);
        lastWound.put(target.name(), dealt);
        events.publish(GameEventType.DAMAGE, target.name(), target.name() + " received " + dealt + " damage", dealt);
        publishHealth(target);

        int healed = dealt * attack.lifestealPercent() / 100 + attack.selfHeal();
        if (healed > 0 && attacker.isAlive()) {
            attacker.heal(healed);
            events.publish(GameEventType.HEAL, attacker.name(), attacker.name() + " restored " + healed + " health", healed);
            publishHealth(attacker);
        }
        if (attack.rewindPercent() > 0 && attacker.isAlive()) rewind(attacker, attack.rewindPercent());
        if (!target.isAlive()) {
            events.publish(GameEventType.DEFEATED, target.name(), target.name() + " was defeated", 0);
        }
        return dealt;
    }

    public int lastWound(Combatant combatant) { return lastWound.getOrDefault(combatant.name(), 0); }

    private void rewind(Combatant attacker, int percent) {
        int restored = lastWound(attacker) * percent / 100;
        lastWound.put(attacker.name(), 0);
        attacker.heal(restored);
        events.publish(GameEventType.HEAL, attacker.name(), restored > 0
                ? attacker.name() + " rewinds time and undoes " + restored + " damage"
                : "There is no wound for " + attacker.name() + " to rewind", restored);
        if (restored > 0) publishHealth(attacker);
    }

    private void sendBack(Combatant attacker, Combatant target, int swallowed) {
        int returned = swallowed * portal.returnPercent() / 100;
        events.publish(GameEventType.PORTAL, target.name(),
                "Portal! " + attacker.name() + " is hit by its own blow", returned);
        if (returned == 0) return;
        attacker.takeDamage(returned);
        lastWound.put(attacker.name(), returned);
        events.publish(GameEventType.DAMAGE, attacker.name(), attacker.name() + " received " + returned + " damage", returned);
        publishHealth(attacker);
        if (!attacker.isAlive()) {
            events.publish(GameEventType.DEFEATED, attacker.name(), attacker.name() + " was defeated", 0);
        }
    }

    private void publishHealth(Combatant combatant) {
        events.publish(GameEventType.HEALTH_CHANGED, combatant.name(),
                combatant.name() + " health: " + combatant.health() + "/" + combatant.maxHealth(), combatant.health());
    }
}
