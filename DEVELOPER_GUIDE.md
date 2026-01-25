# Cobblemon Academy Alternative - Developer Guide

This document outlines the custom features, file structures, and modifications present in this fork of Cobblemon (**Cobblemon Academy Alternative**). It is intended for contributors who wish to maintain or expand upon these features.

## 1. Project Overview

This fork includes several custom systems designed for the Cobblemon Academy server, including:
- **Battle Factory / Tower**: A PvE challenge mode with generated teams.
- **Economy & Shop**: A custom NPC shop system selling items for currency.
- **Custom NPCs**: Unique trainers and utility NPCs with custom textures and dialogues.
- **Mega Evolution & Battle Fixes**: Re-enabled Mega Evolution and fixes for battle UI logic.

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

**Data Files:**
- **NPC:** `data/cobblemon/npcs/shopkeeper.json`.
- **Dialogue:** `data/cobblemon/dialogues/shop_welcome.json`.

### C. Custom Textures
We use custom textures for specific NPCs without replacing the default player skin.

**Texture Locations:**
- Images: `common/src/main/resources/assets/cobblemon/textures/npcs/standard/`
  - `trainertower.png`
  - `shopkeeper.png`

**Definition Logic:**
- **Texture Mapping:** [`assets/cobblemon/bedrock/npcs/variations/standard/99_custom_npcs.json`](common/src/main/resources/assets/cobblemon/bedrock/npcs/variations/standard/99_custom_npcs.json).
  - Maps aspects (`tower-trainer-custom`, `shopkeeper-custom`) to the PNG files.
- **Applying to NPCs:**
  - In the NPC JSON (e.g., `shopkeeper.json`), add `"aspects": ["shopkeeper-custom"]`.

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
- **Textures not showing?** Check `99_custom_npcs.json` to ensure the aspect name matches exactly what is in the NPC file, and that the path to the PNG is correct relative to `assets/`.
- **NPC interactions broken?** Validate the JSON syntax in `dialogues/`. A missing comma can prevent the dialogue from loading.
