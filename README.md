# Scotland Yard - COMS10018 Coursework

A Java implementation of the Scotland Yard board game for the COMS10018 Object-Oriented Programming course at the University of Bristol.

## Overview

Scotland Yard is a strategic board game where detectives work together to catch the mysterious Mr. X as he moves secretly around London. This project implements the game logic and model for the digital version of Scotland Yard.

## What I Implemented

### Stage 1: MyGameStateFactory

Implemented the `GameState` interface that manages the core game logic:

- **Move Generation**
  - `makeSingleMoves()`: Computes all possible single moves for a player based on their location, available tickets, and board connections
  - `makeDoubleMoves()`: Computes double moves available to Mr. X, allowing two consecutive moves in one turn
  - `getAvailableMoves()`: Returns all valid moves for the current player's turn

- **Game State Management**
  - `advance(Move move)`: Processes a move and returns the new game state using the Visitor pattern to handle both `SingleMove` and `DoubleMove` types
  - `getWinner()`: Determines if the game has ended and returns the winner(s)

- **Board Information**
  - `getSetup()`: Returns the game setup configuration
  - `getPlayers()`: Returns all players in the game
  - `getDetectiveLocation()`: Returns the location of a specific detective
  - `getPlayerTickets()`: Returns the ticket board for a player
  - `getMrXTravelLog()`: Returns Mr. X's travel history

### Stage 2: MyModelFactory

Implemented the `Model` interface using the Observer pattern:

- **Observer Management**
  - `registerObserver()`: Registers an observer to receive game events
  - `unregisterObserver()`: Removes an observer
  - `getObservers()`: Returns all registered observers

- **Game Control**
  - `getCurrentBoard()`: Returns the current game board state
  - `chooseMove()`: Advances the game with a chosen move and notifies observers with either `MOVE_MADE` or `GAME_OVER` events

## Design Patterns Used

- **Factory Pattern**: `MyGameStateFactory` and `MyModelFactory` create game instances
- **Visitor Pattern**: Used in `advance()` to handle different move types (`SingleMove` and `DoubleMove`)
- **Observer Pattern**: Used in `MyModel` to notify listeners of game state changes

## Prerequisites

- Java 17 or higher
- Maven 3.x

## Building the Project

```bash
./mvnw compile
```

## Running Tests

```bash
./mvnw test
```

## Running the Game

```bash
./mvnw exec:java
```

This will launch the JavaFX graphical user interface for the Scotland Yard game.

## Project Structure

```
src/
├── main/java/uk/ac/bris/cs/scotlandyard/
│   ├── Main.java                    # Application entry point
│   ├── model/
│   │   ├── MyGameStateFactory.java  # Stage 1 implementation
│   │   ├── MyModelFactory.java      # Stage 2 implementation
│   │   ├── Board.java               # Board interface
│   │   ├── Model.java               # Model interface
│   │   ├── Move.java                # Move classes
│   │   ├── Player.java              # Player class
│   │   └── ...
│   └── ui/                          # User interface components
└── test/java/                       # Test suite
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
