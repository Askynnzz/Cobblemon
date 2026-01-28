# Cobblemon Academy Alternative - Developer Guide

This document outlines the custom features, file structures, and modifications present in this fork of Cobblemon (**Cobblemon Academy Alternative**). It is intended for contributors who wish to maintain or expand upon these features.

## 1. Project Overview

This fork includes several custom systems designed for the Cobblemon Academy server, including:
- **Battle Factory / Tower**: A PvE challenge mode with generated teams.
- **Economy & Shop**: A custom NPC shop system selling items for currency. (Can be disabled through main config from cobblemon to use an other economy mod)
- **Custom NPCs**: Trainers and utility NPCs dialogues. 
- **Mega Evolution & Battle Fixes**: Re-enabled Mega Evolution managed by Mega Showdown and fixes for battle UI logic and ragdgym/raid dens.

---

## 2. Key Systems & Files

### A. Battle Factory / Tower
The Battle Tower allows players to challenge AI trainers with progressively harder teams.

**Configuration:**
- [`common/src/main/resources/config/cobblemon/battle_factory_tower.json`](common/src/main/resources/config/cobblemon/battle_factory_tower.json)
  - Controls tower rules, tiers, potential rental Pokémon, and difficulty scaling.

**Data Files:**
- **Dialogues:**
  - `data/cobblemon/dialogues/battlefactory.json`: General factory interactions.
  - `data/cobblemon/dialogues/tower.json`: Main tower entry/exit text.
  - `data/cobblemon/dialogues/tower_victory.json` & `tower_defeat.json`: Result messages.
  - `data/cobblemon/dialogues/tower_lobby_attendant.json`: Lobby NPC dialogue.
- **NPCs:**
  - `data/cobblemon/npcs/tower_trainer.json`: The opponent NPC entity.
  - `data/cobblemon/npcs/tower_lobby_attendant.json`: The NPC that lets you join the tower.

**Source Code:**
- Package: `com.cobblemon.mod.common.api.battlefactory`
- Key Class: `TowerBattleActor` (Handles AI logic for tower trainers).

**Commands:**
- /towersimulate [difficulty] [complete/fail] to force the end of a trial
- /battlefactory refresh to hard refresh the UI if there is any issues with it after a trial.

### B. Economy & Shop
A simple shop system where players can buy items.

**Configuration:**
- [`config/cobblemon/economy_shop.json`](config/cobblemon/economy_shop.json)
  - Defines the shop inventory.
  - **Format:**
    ```json
    {
      "slot": 13,
      "item": "cobblemon:rare_candy",
      "price": 4800,
      "amount": 1
    }
    ```
  - You can disable it through config/cobblemon/main.json by turning enableEconomy to false
**Data Files:**
- **NPC:** `data/cobblemon/npcs/shopkeeper.json`.
- **Dialogue:** `data/cobblemon/dialogues/shop_welcome.json`.


---

## 3. How to Modify...

### ...The Shop Inventory
1. Open `config/cobblemon/economy_shop.json`.
2. Add a new object to the list.
3. **Important:** Ensure `slot` indices do not overlap.

### ...Tower Teams
1. Open `common/src/main/resources/config/cobblemon/battle_factory_tower.json`.
2. Edit the `pokemon` lists under each tier (e.g., `rental`, `opponent`).
3. You can define sets using Showdown format strings.

### ...Mod Metadata (Name, Icon, Description)
- **Fabric:** `fabric/src/main/resources/fabric.mod.json`
- **NeoForge:** `neoforge/src/main/resources/META-INF/neoforge.mods.toml`
- **Icon:** `common/src/main/resources/assets/cobblemon/icon_cobblemon.png`

---

## 4. Troubleshooting
- **NPC interactions broken?** Validate the JSON syntax in `dialogues/`. A missing comma can prevent the dialogue from loading.
- **Pokemon GUI not refreshing after a battle/after battle factory?** Disconnect and reconnect to your server should be enough to fix this for the moment , or try /battlefactory refresh.
