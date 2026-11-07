package com.thanos.participant3.patterns.decorator;

import com.thanos.participant3.Combatant;

/** Hook for HitResolver: may swallow a hit and return part of it to the attacker. */
public interface Portal {
    Portal NONE = target -> false;

    boolean opensFor(Combatant target);

    default int returnPercent() { return 0; }
}
