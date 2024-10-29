/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * An [EvolutionRequirement] for when the party needs to either contain or not a specific match for [target] based on the [contains] property.
 *
 * @property target The matcher for the party members.
 * @property contains If this requirement will need the [target] to be present or not.
 * @author Licious
 * @since March 21st, 2022
 */
class PartyMemberRequirement : EvolutionRequirement {
    companion object {
        const val ADAPTER_VARIANT = "party_member"
    }

    val target = PokemonProperties()
    val type: String? = null
    val gender: String? = null
    val contains = true
    override fun check(pokemon: Pokemon): Boolean {
        val party = pokemon.storeCoordinates.get()?.store as? PartyStore ?: return false
        var has = party.any { member -> member.uuid != pokemon.uuid && this.target.matches(member) }
        if (type != null) {
            has = party.any { member -> member.uuid != pokemon.uuid && member.types.any { it.name.equals(type, true) } }
        }
        if (gender != null) {
            has = pokemon.gender.name.equals(gender, true)
        }
        return this.contains == has
    }
}