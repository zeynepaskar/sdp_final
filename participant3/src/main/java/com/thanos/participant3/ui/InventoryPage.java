package com.thanos.participant3.ui;

import com.thanos.participant3.Combatant;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.progress.Thanos;
import com.thanos.participant3.stones.Ability;
import java.util.List;

/** Inventory menu state without rendering, so it can be tested. */
public final class InventoryPage {
    public static final int VISIBLE_ROWS = 11;
    private final Inventory inventory;
    private final Combatant thanos;
    private int cursor;
    private int firstRow;

    public InventoryPage(Inventory inventory, Combatant thanos) {
        if (inventory == null || thanos == null) {
            throw new IllegalArgumentException("Inventory page needs the inventory and Thanos");
        }
        this.inventory = inventory;
        this.thanos = thanos;
    }

    public Inventory inventory() { return inventory; }
    public Combatant thanos() { return thanos; }
    public Thanos leveled() { return thanos instanceof Thanos leveled ? leveled : null; }
    public List<Ability> abilities() { return inventory.abilities(); }
    public int cursor() { return cursor; }
    public int firstRow() { return firstRow; }

    public int punchDamage() {
        int base = leveled() != null ? leveled().punchDamage() : BattleScreen.PUNCH_DAMAGE;
        return inventory.empower(new BasicAttack("Punch", base)).damage();
    }

    public List<String> stats() {
        return List.of("Punch  " + punchDamage(),
                "Defense  " + thanos.defense(),
                "Energy per turn  +" + inventory.energyPerTurn());
    }

    public void move(int delta) { select(cursor + delta); }

    public void select(int row) {
        int last = Math.max(0, abilities().size() - 1);
        cursor = Math.max(0, Math.min(last, row));
        if (cursor < firstRow) firstRow = cursor;
        if (cursor >= firstRow + VISIBLE_ROWS) firstRow = cursor - VISIBLE_ROWS + 1;
    }

    public void reset() {
        cursor = 0;
        firstRow = 0;
    }
}
