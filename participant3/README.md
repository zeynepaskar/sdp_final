# Participant 3: Gameplay

Gameplay module of *THANOS: THE INFINITY QUEST*:

- Infinity Stones, the inventory and energy;
- Thanos's abilities, levels and XP;
- the HUD and the turn-based battle screen (libGDX).

Design patterns: **Observer** and **Decorator**.

## Patterns

| Pattern | Package | Main classes |
|---|---|---|
| Observer | `patterns.observer` | `EventBus` (subject), `GameEventListener` (observer), `GameEvent`, `GameEventType` |
| Decorator | `patterns.decorator` | `Attack` (component), `BasicAttack`, `AttackDecorator`, 6 stone and 4 ability decorators, `HitResolver` |

**Observer**
- **Publishers:** `HitResolver`, `SimpleTurnBattle`, `Inventory`, `Thanos`.
- **Observers:** `BattleMessages`, `HudModel`, `StoneRewards`, `SoulHarvest`, `ConsoleLog`.

**Decorator**
- Each stone wraps an attack in its decorator (`InfinityStone.empower`).
- `AbilityBook` builds eight skills (two base ones and one per stone), each in a plain and an upgraded form, by stacking decorators. Every stone unlocks its skill and upgrades the next one in line.

The full write-up with code, UML and screenshots is in [docs/report/participant3-report.md](docs/report/participant3-report.md).

## Packages

| Package | What is inside |
|---|---|
| `patterns.observer` | event bus and events |
| `patterns.decorator` | attacks, decorators, `HitResolver`, `Portal` hook |
| `stones` | `InfinityStone`, `Ability`, `AbilityBook`, `SoulHarvest`, `SpacePortal` |
| `inventory` | `Inventory`: stones, energy, ability use |
| `progress` | `Thanos`: levels and XP |
| `battle` | `TurnBattle`, `SimpleTurnBattle`, `BattleState` |
| `ui` | battle screen, HUD, inventory screen, stone reward animation, pixel sprites, backdrop |
| `demo` | `BattleDemo`: a standalone battle demo for this module |

## Build and test

Needs JDK 17+ and Maven. From the repository root:

```
mvn -f participant3/pom.xml package
```

This runs the 72 JUnit 5 tests and builds `participant3/target/participant3-1.0-SNAPSHOT-demo.jar`.

## Run the battle demo

```
participant3\run-battle-demo.bat
```

Or run `java --enable-native-access=ALL-UNNAMED -jar participant3/target/participant3-1.0-SNAPSHOT-demo.jar`.

| Key | Action |
|---|---|
| Arrows or WASD | move in the menu |
| ENTER or SPACE | choose; next message |
| ESC or BACKSPACE | back |
| I or TAB | inventory |
| F1 | give the next stone |
| F2 | +100 XP |
| F3 | fight a regular enemy (Outrider, Chitauri, Sakaaran) |
| F4 | switch to the next boss |
| F10 | exit |

Every event is also printed to the console by the `ConsoleLog` observer.

## Diagrams

PlantUML sources and PNGs are in `docs/uml`:

| File | Diagram |
|---|---|
| `observer.puml` | Observer class diagram |
| `decorator.puml` | Decorator class diagram |
| `hit-sequence.puml` | one hit with an ability: Decorator builds the attack, Observer reports it |

Render them with PlantUML 1.2026.8 or newer (no Graphviz needed):

```
java -jar plantuml.jar -tpng participant3/docs/uml/*.puml
```
