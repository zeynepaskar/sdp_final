# Thanos

Explore six worlds, challenge each guardian, and collect the six Infinity Stones. An enemy that spots Thanos starts a turn-based battle. Defeating the guardian opens that world's gate.

With Java 17+ and Maven, run from the project folder:

```sh
mvn clean package
./run.sh
```

Move with the arrow keys or WASD. Use **E** at an open gate, **Enter** to choose a battle action, and **I** for inventory.

- **Participant 1** builds the maps and coordinates exploration and battles through Builder and Facade.
- **Participant 2** creates the enemies and bosses and selects their attacks through Factory Method and Strategy.
- **Participant 3** provides the battle screen, inventory and stone abilities through Observer and Decorator.
