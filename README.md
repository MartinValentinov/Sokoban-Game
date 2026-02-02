# Sokoban Game

## Overview
This project is a **classic Sokoban game** implemented as a learning exercise in programming.  
The goal of the game is to push all boxes onto the target locations while navigating a maze-like level.  

---

## Gameplay

- The player moves a character in a grid-based warehouse.  
- **Boxes** must be pushed onto **goal squares**.  
- The player can only **push one box at a time** and cannot pull boxes.  
- Walls and other obstacles restrict movement.  
- The game is won when **all boxes are on goal squares**.  

---

## Features

- Multiple levels with increasing difficulty  
- Grid-based movement  
- Collision detection with walls and boxes  
- Move counter to track player efficiency  
- Optional: Undo moves

---

## Controls

- Arrow keys or WASD keys to move the player  

---

## How to Run

```bash
# Compile the game
g++ -o sokoban src/main.cpp src/game.cpp

# Run the game
./sokoban
```
