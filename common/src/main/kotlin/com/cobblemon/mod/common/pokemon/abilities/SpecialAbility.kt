/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.abilities

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.cobblemon.mod.common.api.abilities.PotentialAbility
import com.cobblemon.mod.common.api.abilities.PotentialAbilityType
import com.google.gson.JsonElement

object SpecialAbilityType : PotentialAbilityType<SpecialAbility> {
    override fun parseFromJSON(element: JsonElement): SpecialAbility? {
        val str = if (element.isJsonPrimitive) element.asString else null
        return if (str?.startsWith("s:") == true) {
            val abilityString = str.substringAfter("s:")
            val ability = Abilities.get(abilityString)
            if (ability != null) {
                SpecialAbility(ability)
            } else {
                Cobblemon.LOGGER.error("Special ability referred to unknown ability: $abilityString")
                null
            }
        } else {
            null
        }
    }
}

/**
 * Abilities for Pokémon that are obtained through special means.
 * @author Deltric
 * @since November 28th, 2023
 */
class SpecialAbility(override val template: AbilityTemplate) : PotentialAbility {
    override val priority: Priority = Priority.LOW
    override val type = SpecialAbilityType

    override fun isSatisfiedBy(aspects: Set<String>) = false
}