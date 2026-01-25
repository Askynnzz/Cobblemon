/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Temporary cache for storing offered Pokemon during Tower selection process.
 * 
 * When a player starts a Tower session, 6 Pokemon are generated and stored here.
 * The GUI is opened on the client side, and when the player confirms their selection,
 * the client sends back the selected indices, which are used to retrieve the Pokemon
 * from this cache.
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
object TowerPokemonCache {
    
    private val cache = ConcurrentHashMap<UUID, List<Pokemon>>()
    
    /**
     * Stores the offered Pokemon for a player.
     */
    fun store(playerUUID: UUID, pokemon: List<Pokemon>) {
        cache[playerUUID] = pokemon
    }
    
    /**
     * Retrieves the offered Pokemon for a player.
     */
    fun get(playerUUID: UUID): List<Pokemon>? {
        return cache[playerUUID]
    }
    
    /**
     * Removes the offered Pokemon for a player from cache.
     */
    fun remove(playerUUID: UUID) {
        cache.remove(playerUUID)
    }
    
    /**
     * Checks if player has cached Pokemon.
     */
    fun has(playerUUID: UUID): Boolean {
        return cache.containsKey(playerUUID)
    }
}
