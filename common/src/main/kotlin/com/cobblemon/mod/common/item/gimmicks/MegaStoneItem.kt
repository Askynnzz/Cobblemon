/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.gimmicks

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity

class MegaStoneItem(val evolvablePokemon: String, val aspect: String) : Item(Properties().rarity(Rarity.RARE).stacksTo(1).fireResistant()) {

    fun canMegaEvolve(pokemon: Pokemon): Boolean {
        return pokemon.showdownId() == evolvablePokemon
    }

    fun megaEvolve(pokemon: PokemonEntity) {
        if (!canMegaEvolve(pokemon.pokemon)) return
        PokemonProperties.parse(aspect).apply(pokemon)
    }

}