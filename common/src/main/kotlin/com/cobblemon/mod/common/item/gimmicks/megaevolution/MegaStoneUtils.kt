/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.gimmicks.megaevolution

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.item.gimmicks.MegaStoneItem
import com.cobblemon.mod.common.pokemon.Pokemon

object MegaStoneUtils {

    val MEGA_ASPECTS = listOf("mega", "mega_x", "mega_y")

    fun canApplyMegaEvolution(pokemon: Pokemon): Boolean {
        val heldItem = pokemon.heldItem().item
        return heldItem is MegaStoneItem && heldItem.canMegaEvolve(pokemon)
    }

    fun canRemoveMegaEvolution(pokemon: Pokemon): Boolean {
        return MEGA_ASPECTS.any { pokemon.aspects.contains(it) }
    }

    fun removeMegaEvolution(pokemon: Pokemon) {
        if (canRemoveMegaEvolution(pokemon)) {
            PokemonProperties.parse("mega_evolution=none").apply(pokemon)
        }
    }

}