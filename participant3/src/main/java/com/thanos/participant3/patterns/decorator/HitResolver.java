package com.thanos.participant3.patterns.decorator;

import com.thanos.participant3.Combatant;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEventType;

/**
 * Applies a decorated attack to a target and reports what happened as events.
 * Blocking, slow and stun are left to the combat system, which reads them from the attack.
 */
public final class HitResolver {
    private final EventBus events;

    public HitResolver(EventBus events) {
        if (events == null) {
            throw new IllegalArgumentException("Resolver needs an event bus");
        }
        this.events = events;
    }

    /** Returns the damage actually dealt. */
    public int resolve(Combatant attacker, Combatant target, Attack attack) {
        if (attacker == null || target == null || attack == null) {
            throw new IllegalArgumentException("Attacker, target and attack are required");
        }
        int dealt = attack.ignoresDefense() ? attack.damage() : Math.max(0, attack.damage() - target.defense());
        target.takeDamage(dealt);
        events.publish(GameEventType.DAMAGE, target.name(), target.name() + " received " + dealt + " damage", dealt);
        publishHealth(target);

        int healed = dealt * attack.lifestealPercent() / 100;
        if (healed > 0 && attacker.isAlive()) {
            attacker.heal(healed);
            events.publish(GameEventType.HEAL, attacker.name(), attacker.name() + " restored " + healed + " health", healed);
            publishHealth(attacker);
        }
        if (!target.isAlive()) {
            events.publish(GameEventType.DEFEATED, target.name(), target.name() + " was defeated", 0);
        }
        return dealt;
    }

    private void publishHealth(Combatant combatant) {
        events.publish(GameEventType.HEALTH_CHANGED, combatant.name(),
                combatant.name() + " health: " + combatant.health() + "/" + combatant.maxHealth(), combatant.health());
    }
}
