/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.annotations.SerializedName
import net.minecraft.resources.ResourceLocation

/**
 * Preset definition for a single rental Pokémon.
 * This defines all the stats, moves, and properties that a rental Pokémon should have.
 * 
 * @property species The species identifier (e.g. "cobblemon:pikachu")
 * @property level The level of the Pokémon (default: 50)
 * @property moves List of move identifiers this Pokémon knows
 * @property ability The ability identifier
 * @property item Optional held item identifier
 * @property evSpread EV distribution across stats
 * @property ivSpread IV distribution across stats (default: all 31)
 * @property nature The nature identifier
 * @property shiny Whether this Pokémon is shiny (default: false)
 * @property gender Optional gender ("male", "female", or null for genderless)
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
data class PresetPokemon(
    @SerializedName("species")
    val species: String,
    
    @SerializedName("level")
    val level: Int = 50,
    
    @SerializedName("moves")
    val moves: List<String>,
    
    @SerializedName("ability")
    val ability: String,
    
    @SerializedName("item")
    val item: String? = null,
    
    @SerializedName("evSpread")
    val evSpread: Map<String, Int> = mapOf(
        "hp" to 0,
        "attack" to 0,
        "defence" to 0,
        "special_attack" to 0,
        "special_defence" to 0,
        "speed" to 0
    ),
    
    @SerializedName("ivSpread")
    val ivSpread: Map<String, Int> = mapOf(
        "hp" to 31,
        "attack" to 31,
        "defence" to 31,
        "special_attack" to 31,
        "special_defence" to 31,
        "speed" to 31
    ),
    
    @SerializedName("nature")
    val nature: String = "hardy",
    
    @SerializedName("shiny")
    val shiny: Boolean = false,
    
    @SerializedName("gender")
    val gender: String? = null
) {
    
    /**
     * Creates a real Pokemon object from this preset.
     */
    fun toPokemon(): Pokemon? {
        return try {
            // Parse species
           val speciesIdentifier = ResourceLocation.parse(species)
            val speciesObj = com.cobblemon.mod.common.api.pokemon.PokemonSpecies.getByIdentifier(speciesIdentifier)
                ?: return null
            
            // Create Pokemon
            val pokemon = Pokemon().apply {
                this.species = speciesObj
                this.level = this@PresetPokemon.level
                this.shiny = this@PresetPokemon.shiny
                
                // Set gender if specified
                this@PresetPokemon.gender?.let { genderStr ->
                    when (genderStr.lowercase()) {
                        "male" -> this.gender = com.cobblemon.mod.common.pokemon.Gender.MALE
                        "female" -> this.gender = com.cobblemon.mod.common.pokemon.Gender.FEMALE
                    }
                }
                
                // Set nature
                val natureObj = Natures.getNature(ResourceLocation.parse(this@PresetPokemon.nature))
                if (natureObj != null) {
                    this.nature = natureObj
                }
                
                // Set ability
                val abilityTemplate = this.form.abilities.mapping.values.flatten()
                    .find { it.template.name.equals(this@PresetPokemon.ability, ignoreCase = true) }
                if (abilityTemplate != null) {
                    this.ability = abilityTemplate.template.create(false)
                }
                
                // Set moves
                this.moveSet.clear()
                this@PresetPokemon.moves.forEach { moveId ->
                    val move = com.cobblemon.mod.common.api.moves.Moves.getByName(moveId)
                    if (move != null) {
                        this.moveSet.add(move.create())
                    }
                }
                
                // Set IVs
                this@PresetPokemon.ivSpread.forEach { (statName, value) ->
                    getStatFromName(statName)?.let { stat ->
                        this.setIV(stat, value)
                    }
                }
                
                // Set EVs
                this@PresetPokemon.evSpread.forEach { (statName, value) ->
                    getStatFromName(statName)?.let { stat ->
                        this.setEV(stat, value)
                    }
                }
                
                // Set held item
                this@PresetPokemon.item?.let { itemId ->
                    try {
                        val itemStack = net.minecraft.world.item.ItemStack(
                            net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId))
                        )
                        if (!itemStack.isEmpty) {
                            this.swapHeldItem(itemStack, false)
                        }
                    } catch (e: Exception) {
                        // Item not found, skip
                    }
                }
                
                // Initialize Pokemon
                this.initialize()
            }
            
            pokemon
            
        } catch (e: Exception) {
            com.cobblemon.mod.common.Cobblemon.LOGGER.error("Failed to create Pokemon from preset: $species", e)
            null
        }
    }
    
    /**
     * Maps stat name strings to Stat enums.
     */
    private fun getStatFromName(name: String): Stat? {
        return when (name.lowercase()) {
            "hp" -> Stats.HP
            "attack" -> Stats.ATTACK
            "defence", "defense" -> Stats.DEFENCE
            "special_attack", "sp_attack", "spatk" -> Stats.SPECIAL_ATTACK
            "special_defence", "special_defense", "sp_defence", "sp_defense", "spdef" -> Stats.SPECIAL_DEFENCE
            "speed" -> Stats.SPEED
            else -> null
        }
    }
}
