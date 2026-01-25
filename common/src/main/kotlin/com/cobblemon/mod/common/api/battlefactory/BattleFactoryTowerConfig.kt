/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import com.google.gson.JsonObject
import net.minecraft.core.BlockPos

/**
 * Represents a reward set that can be given to a player.
 * 
 * @property items List of item strings in format "item_id:count"
 */
data class RewardSet(
    val items: List<String>
) {
    companion object {
        fun fromJson(json: JsonObject): RewardSet {
            val itemsArray = json.getAsJsonArray("items")
            val items = itemsArray.map { it.asString }
            return RewardSet(items)
        }
    }
}

/**
 * Represents a difficulty level for the Battle Factory Tower.
 * 
 * @property id Unique identifier (e.g., "easy", "normal", "hard")
 * @property displayName Formatted display name with color codes
 * @property pokemonLevelMin Minimum Pokémon level for this difficulty
 * @property pokemonLevelMax Maximum Pokémon level for this difficulty
 * @property trainerAiDifficulty AI difficulty level
 * @property rewardMultiplier Multiplier for rewards
 * @property completionReward Rewards given upon completing all 7 arenas
 */
data class TowerDifficulty(
    val id: String,
    val displayName: String,
    val pokemonLevelMin: Int,
    val pokemonLevelMax: Int,
    val trainerAiDifficulty: String,
    val rewardMultiplier: Double,
    val completionReward: RewardSet
) {
    companion object {
        fun fromJson(json: JsonObject): TowerDifficulty {
            val levelRange = json.getAsJsonArray("pokemon_level_range")
            return TowerDifficulty(
                id = json.get("id").asString,
                displayName = json.get("display_name").asString,
                pokemonLevelMin = levelRange[0].asInt,
                pokemonLevelMax = levelRange[1].asInt,
                trainerAiDifficulty = json.get("trainer_ai_difficulty").asString,
                rewardMultiplier = json.get("reward_multiplier").asDouble,
                completionReward = RewardSet.fromJson(json.getAsJsonObject("completion_reward"))
            )
        }
    }
}

/**
 * Represents a Battle Factory arena location.
 * 
 * @property id Unique arena identifier
 * @property name Display name
 * @property world World name
 * @property coordinates Arena center coordinates
 * @property trainerSpawn Where to spawn the NPC trainer
 * @property playerSpawn Where to teleport the player
 */
data class ArenaLocation(
    val id: String,
    val name: String,
    val world: String,
    val coordinates: BlockPos,
    val trainerSpawn: BlockPos,
    val playerSpawn: BlockPos
) {
    companion object {
        fun fromJson(json: JsonObject): ArenaLocation {
            fun parseBlockPos(obj: JsonObject): BlockPos {
                return BlockPos(
                    obj.get("x").asInt,
                    obj.get("y").asInt,
                    obj.get("z").asInt
                )
            }
            
            return ArenaLocation(
                id = json.get("id").asString,
                name = json.get("name").asString,
                world = json.getAsJsonObject("coordinates").get("world").asString,
                coordinates = parseBlockPos(json.getAsJsonObject("coordinates")),
                trainerSpawn = parseBlockPos(json.getAsJsonObject("trainer_spawn")),
                playerSpawn = parseBlockPos(json.getAsJsonObject("player_spawn"))
            )
        }
    }
}

/**
 * Complete configuration for the Battle Factory Tower system.
 * 
 * @property towerName Display name of the tower
 * @property difficulties List of available difficulty levels (typically 3)
 * @property arenas List of arena locations (typically 7)
 * @property lobbyNpcSpawn Where the lobby NPC should spawn
 */
data class BattleFactoryTowerConfig(
    val towerName: String,
    val difficulties: List<TowerDifficulty>,
    val arenas: List<ArenaLocation>,
    val lobbyWorld: String,
    val lobbyNpcSpawn: BlockPos
) {
    /**
     * Gets a difficulty by its ID.
     */
    fun getDifficulty(id: String): TowerDifficulty? {
        return difficulties.find { it.id == id }
    }
    
    /**
     * Gets an arena by index (0-6 typically).
     */
    fun getArena(index: Int): ArenaLocation? {
        return arenas.getOrNull(index)
    }
    
    companion object {
        fun fromJson(json: JsonObject): BattleFactoryTowerConfig {
            fun parseBlockPos(obj: JsonObject): BlockPos {
                return BlockPos(
                    obj.get("x").asInt,
                    obj.get("y").asInt,
                    obj.get("z").asInt
                )
            }
            
            val difficultiesArray = json.getAsJsonArray("difficulties")
            val difficulties = difficultiesArray.map { TowerDifficulty.fromJson(it.asJsonObject) }
            
            val arenasArray = json.getAsJsonArray("arenas")
            val arenas = arenasArray.map { ArenaLocation.fromJson(it.asJsonObject) }
            
            val lobbyObj = json.getAsJsonObject("lobby_npc_spawn")
            
            return BattleFactoryTowerConfig(
                towerName = json.get("tower_name").asString,
                difficulties = difficulties,
                arenas = arenas,
                lobbyWorld = lobbyObj.get("world").asString,
                lobbyNpcSpawn = parseBlockPos(lobbyObj)
            )
        }
    }
}
