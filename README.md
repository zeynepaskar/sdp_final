# Thanos

Explore six worlds, challenge each guardian, and collect the six Infinity Stones. An enemy that spots Thanos starts a turn-based battle. Defeating the guardian opens that world's gate.

With Java 17+ and Maven, run from the project folder:

```sh
mvn clean package
./run.sh
```

Use the **arrow keys** to move or select, **Z** to interact or confirm, **X** to go back or pause, and **I** for inventory.

## The Last Signal

Look for the observatory entrance just northeast of the landing point on Titan. Gamora recognizes a voice on an old receiver. The trail continues at the salvage dock on Knowhere, where a damaged copy can also get you started. Both stops are optional.

A story marker binds a map symbol and a sprite to a **Command**. For example, `talk` opens a conversation and `travel` enters an area. The exploration, reading, and choice **States** decide what Z and X do. X closes an unanswered choice so you can come back to it.

## Team modules

- **Participant 1** builds maps and places story markers with Builder. GameFacade coordinates movement, story interactions, and battles.
- **Participant 2** creates the enemies and bosses and selects their attacks through Factory Method and Strategy.
- **Participant 3** provides the battle screen, inventory and stone abilities through Observer and Decorator.
