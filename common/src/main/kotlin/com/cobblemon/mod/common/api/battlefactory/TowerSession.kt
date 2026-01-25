/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.UUID

/**
 * Represents an active Battle Factory Tower session for a player.
 * 
 * This tracks a player's progress through the 7 arenas of a tower
 * at a specific difficulty level.
 * 
 * @property playerUUID Player's UUID
 * @property difficulty Selected difficulty level
 * @property selectedPokemon The 3 Pokémon chosen by the player
 * @property currentArena Current arena index (0-6, where 7 = completed)
 * @property wins Total wins in this session
 * @property startTime When the session started (millis)
 * @property currentTrainerNPC Currently spawned trainer NPC (for cleanup)
 */
data class TowerSession(
    val playerUUID: UUID,
    val difficulty: TowerDifficulty,
    val selectedPokemon: List<Pokemon>,
    var currentArena: Int = 0,
    var wins: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    var currentTrainerNPC: UUID? = null
) {
    /**
     * Advances to the next arena.
     */
    fun nextArena() {
        currentArena++
        wins++
    }
    
    /**
     * Checks if the tower has been completed (all 7 arenas beaten).
     */
    fun isComplete(): Boolean {
        return currentArena >= 7
    }
    
    /**
     * Gets the current arena number (1-7) for display.
     */
    fun getCurrentArenaDisplay(): Int {
        return currentArena + 1
    }
    
    /**
     * Calculates session duration in seconds.
     */
    fun getDurationSeconds(): Long {
        return (System.currentTimeMillis() - startTime) / 1000
    }
}
