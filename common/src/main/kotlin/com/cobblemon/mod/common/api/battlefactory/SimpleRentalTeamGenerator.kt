/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader

/**
 * Simple implementation of RentalTeamGenerator that uses JSON presets.
 * 
 * Loads Pokémon definitions from a JSON file and generates random selections.
 * 
 * @property presets List of preset Pokemon configurations to choose from
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
class SimpleRentalTeamGenerator(
    private val presets: List<PresetPokemon>
) : RentalTeamGenerator {
    
    override fun generateOptions(count: Int): List<Pokemon> {
        if (presets.isEmpty()) {
            Cobblemon.LOGGER.error("No presets available for rental team generation")
            return emptyList()
        }
        
        // Select random presets
        val selectedPresets = presets.shuffled().take(count)
        
        // Convert to Pokemon objects
        val pokemonList = selectedPresets.mapNotNull { preset ->
            preset.toPokemon()
        }
        
        if (pokemonList.size < count) {
            Cobblemon.LOGGER.warn("Could only generate ${pokemonList.size} rental Pokemon out of requested $count")
        }
        
        return pokemonList
    }
    
    override fun selectTeam(options: List<Pokemon>, count: Int): List<Pokemon> {
        // V1: Simple random selection server-side
        return options.shuffled().take(count.coerceAtMost(options.size))
    }
    
    companion object {
        private val GSON = Gson()
        private const val PRESETS_FILE_PATH = "config/cobblemon/battle_factory_presets.json"
        
        /**
         * Loads presets from JSON file and creates a generator.
         * If file doesn't exist, creates a default one with examples.
         */
        fun loadFromFile(): SimpleRentalTeamGenerator {
            val file = File(PRESETS_FILE_PATH)
            file.parentFile?.mkdirs()
            
            return if (file.exists()) {
                try {
                    val reader = FileReader(file)
                    val listType = object : TypeToken<List<PresetPokemon>>() {}.type
                    val presets: List<PresetPokemon> = GSON.fromJson(reader, listType)
                    reader.close()
                    
                    Cobblemon.LOGGER.info("Loaded ${presets.size} Battle Factory presets from $PRESETS_FILE_PATH")
                    SimpleRentalTeamGenerator(presets)
                } catch (e: Exception) {
                    Cobblemon.LOGGER.error("Failed to load Battle Factory presets", e)
                    SimpleRentalTeamGenerator(getDefaultPresets())
                }
            } else {
                // Create default file
                Cobblemon.LOGGER.info("Creating default Battle Factory presets file at $PRESETS_FILE_PATH")
                val defaultPresets = getDefaultPresets()
                try {
                    val writer = file.writer()
                    GSON.toJson(defaultPresets, writer)
                    writer.close()
                    Cobblemon.LOGGER.info("Created default presets file with ${defaultPresets.size} Pokemon")
                } catch (e: Exception) {
                    Cobblemon.LOGGER.error("Failed to create default presets file", e)
                }
                SimpleRentalTeamGenerator(defaultPresets)
            }
        }
        
        /**
         * Returns a default set of presets for initial setup.
         */
        private fun getDefaultPresets(): List<PresetPokemon> {
            return listOf(
                PresetPokemon(
                    species = "cobblemon:pikachu",
                    level = 50,
                    moves = listOf("thunderbolt", "quickattack", "irontail", "thunderwave"),
                    ability = "static",
                    item = "cobblemon:light_ball",
                    evSpread = mapOf("special_attack" to 252, "speed" to 252, "hp" to 4),
                    nature = "timid"
                ),
                PresetPokemon(
                    species = "cobblemon:charizard",
                    level = 50,
                    moves = listOf("flareblitz", "airslash", "roost", "dragondance"),
                    ability = "blaze",
                    item = "minecraft:charcoal",
                    evSpread = mapOf("attack" to 252, "speed" to 252, "hp" to 4),
                    nature = "jolly"
                ),
                PresetPokemon(
                    species = "cobblemon:blastoise",
                    level = 50,
                    moves = listOf("surf", "icebeam", "rapidspin", "earthquake"),
                    ability = "torrent",
                    evSpread = mapOf("special_attack" to 252, "hp" to 252, "defence" to 4),
                    nature = "modest"
                ),
                PresetPokemon(
                    species = "cobblemon:venusaur",
                    level = 50,
                    moves = listOf("gigadrain", "sludgebomb", "synthesis", "sleeppowder"),
                    ability = "overgrow",
                    evSpread = mapOf("special_attack" to 252, "hp" to 252, "defence" to 4),
                    nature = "calm"
                ),
                PresetPokemon(
                    species = "cobblemon:gengar",
                    level = 50,
                    moves = listOf("shadowball", "sludgebomb", "focusblast", "substitute"),
                    ability = "cursed_body",
                    evSpread = mapOf("special_attack" to 252, "speed" to 252, "hp" to 4),
                    nature = "timid"
                ),
                PresetPokemon(
                    species = "cobblemon:garchomp",
                    level = 50,
                    moves = listOf("earthquake", "outrage", "stoneedge", "swordsdance"),
                    ability = "rough_skin",
                    evSpread = mapOf("attack" to 252, "speed" to 252, "hp" to 4),
                    nature = "jolly"
                ),
                PresetPokemon(
                    species = "cobblemon:lucario",
                    level = 50,
                    moves = listOf("closecombat", "extremespeed", "bulletpunch", "swordsdance"),
                    ability = "justified",
                    evSpread = mapOf("attack" to 252, "speed" to 252, "hp" to 4),
                    nature = "adamant"
                ),
                PresetPokemon(
                    species = "cobblemon:tyranitar",
                    level = 50,
                    moves = listOf("stoneedge", "crunch", "earthquake", "dragondance"),
                    ability = "sand_stream",
                    evSpread = mapOf("attack" to 252, "hp" to 252, "special_defence" to 4),
                    nature = "adamant"
                ),
                PresetPokemon(
                    species = "cobblemon:dragonite",
                    level = 50,
                    moves = listOf("outrage", "extremespeed", "earthquake", "dragondance"),
                    ability = "multiscale",
                    evSpread = mapOf("attack" to 252, "speed" to 252, "hp" to 4),
                    nature = "adamant"
                ),
                PresetPokemon(
                    species = "cobblemon:metagross",
                    level = 50,
                    moves = listOf("meteormash", "earthquake", "zenheadbutt", "bulletpunch"),
                    ability = "clear_body",
                    evSpread = mapOf("attack" to 252, "hp" to 252, "special_defence" to 4),
                    nature = "adamant"
                )
            )
        }
    }
}
