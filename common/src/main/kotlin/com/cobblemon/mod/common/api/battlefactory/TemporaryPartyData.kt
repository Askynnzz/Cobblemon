/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import java.time.Instant
import java.util.UUID

/**
 * Data structure holding the backup of a player's original party and their current rental team.
 * This is persisted to ensure party restoration even after crashes or disconnections.
 *
 * @property originalParty The player's original Pokémon party that was backed up
 * @property rentalParty The current rental Pokémon being used (optional, for tracking)
 * @property sessionId Unique identifier for this temporary party session
 * @property startTime When the backup was created
 * @property wins Number of wins in the current Battle Factory streak
 *
 * @author Cobblemon Contributors
 * @since January 2026
 */
data class TemporaryPartyData(
    val originalParty: List<Pokemon>,
    val rentalParty: List<Pokemon> = emptyList(),
    val sessionId: UUID,
    val startTime: Instant,
    val wins: Int = 0
) {
    
    /**
     * Saves this data to NBT format for persistence.
     */
    fun saveToNBT(registryAccess: RegistryAccess): CompoundTag {
        val nbt = CompoundTag()
        
        // Save session metadata
        nbt.putUUID(SESSION_ID_KEY, sessionId)
        nbt.putLong(START_TIME_KEY, startTime.toEpochMilli())
        nbt.putInt(WINS_KEY, wins)
        
        // Save original party
        val originalList = ListTag()
        originalParty.forEach { pokemon ->
            val pokemonNBT = pokemon.saveToNBT(registryAccess)
            originalList.add(pokemonNBT)
        }
        nbt.put("originalParty", originalList)
        
        // Save rental party
        if (rentalParty.isNotEmpty()) {
            val rentalList = ListTag()
            rentalParty.forEach { pokemon ->
                val pokemonNBT = pokemon.saveToNBT(registryAccess)
                rentalList.add(pokemonNBT)
            }
            nbt.put("rentalParty", rentalList)
        }
        
        return nbt
    }
    
    /**
     * Saves this data to JSON format for persistence.
     */
    fun saveToJSON(registryAccess: RegistryAccess): JsonObject {
        val json = JsonObject()
        
        // Save session metadata
        json.addProperty(SESSION_ID_KEY, sessionId.toString())
        json.addProperty(START_TIME_KEY, startTime.toEpochMilli())
        json.addProperty(WINS_KEY, wins)
        
        // Save original party
        val originalArray = JsonArray()
        originalParty.forEach { pokemon ->
            val pokemonJSON = pokemon.saveToJSON(registryAccess)
            originalArray.add(pokemonJSON)
        }
        json.add("originalParty", originalArray)
        
        // Save rental party
        if (rentalParty.isNotEmpty()) {
            val rentalArray = JsonArray()
            rentalParty.forEach { pokemon ->
                val pokemonJSON = pokemon.saveToJSON(registryAccess)
                rentalArray.add(pokemonJSON)
            }
            json.add("rentalParty", rentalArray)
        }
        
        return json
    }
    
    companion object {
        private const val SESSION_ID_KEY = "SessionId"
        private const val START_TIME_KEY = "StartTime"
        private const val WINS_KEY = "Wins"
        private const val ORIGINAL_PARTY_KEY = "OriginalParty"
        private const val RENTAL_PARTY_KEY = "RentalParty"
        
        /**
         * Loads TemporaryPartyData from NBT format.
         */
        fun loadFromNBT(nbt: CompoundTag, registryAccess: RegistryAccess): TemporaryPartyData? {
            return try {
                val sessionId = nbt.getUUID(SESSION_ID_KEY)
                val startTime = Instant.ofEpochMilli(nbt.getLong(START_TIME_KEY))
                val wins = nbt.getInt(WINS_KEY)
                
                // Load original party
                val originalList = nbt.getList("originalParty", 10) // 10 = CompoundTag type
                val originalParty = mutableListOf<Pokemon>()
                for (i in 0 until originalList.size) {
                    val pokemonNBT = originalList.getCompound(i)
                    val pokemon = Pokemon().loadFromNBT(registryAccess, pokemonNBT)
                    originalParty.add(pokemon)
                }
                
                // Load rental party
                val rentalParty = if (nbt.contains("rentalParty")) {
                    val rentalList = nbt.getList("rentalParty", 10)
                    val rental = mutableListOf<Pokemon>()
                    for (i in 0 until rentalList.size) {
                        val pokemonNBT = rentalList.getCompound(i)
                        val pokemon = Pokemon().loadFromNBT(registryAccess, pokemonNBT)
                        rental.add(pokemon)
                    }
                    rental
                } else {
                    emptyList()
                }
                
                TemporaryPartyData(
                    originalParty = originalParty,
                    rentalParty = rentalParty,
                    sessionId = sessionId,
                    startTime = startTime,
                    wins = wins
                )
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * Loads TemporaryPartyData from JSON format.
         */
        fun loadFromJSON(json: JsonObject, registryAccess: RegistryAccess): TemporaryPartyData? {
            return try {
                val sessionId = UUID.fromString(json.get(SESSION_ID_KEY).asString)
                val startTime = Instant.ofEpochMilli(json.get(START_TIME_KEY).asLong)
                val wins = json.get(WINS_KEY).asInt
                
                // Load original party
                val originalArray = json.getAsJsonArray("originalParty")
                val originalParty = mutableListOf<Pokemon>()
                originalArray.forEach { element ->
                    val pokemon = Pokemon().loadFromJSON(registryAccess, element.asJsonObject)
                    originalParty.add(pokemon)
                }
                
                // Load rental party
                val rentalParty = if (json.has("rentalParty")) {
                    val rentalArray = json.getAsJsonArray("rentalParty")
                    val rental = mutableListOf<Pokemon>()
                    rentalArray.forEach { element ->
                        val pokemon = Pokemon().loadFromJSON(registryAccess, element.asJsonObject)
                        rental.add(pokemon)
                    }
                    rental
                } else {
                    emptyList()
                }
                
                TemporaryPartyData(
                    originalParty = originalParty,
                    rentalParty = rentalParty,
                    sessionId = sessionId,
                    startTime = startTime,
                    wins = wins
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
