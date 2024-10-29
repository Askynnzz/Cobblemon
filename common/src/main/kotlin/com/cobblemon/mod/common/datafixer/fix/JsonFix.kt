/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer.fix

import com.cobblemon.mod.common.util.DataKeys
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic

class JsonFix(outputSchema: Schema) : PokemonFix(outputSchema) {

    override fun fixPokemonData(dynamic: Dynamic<*>): Dynamic<*> {
        var baseDynamic = dynamic
        baseDynamic.get(DataKeys.POKEMON_STATE).result().ifPresent { state ->
            if (!state.get(DataKeys.POKEMON_STATE_TYPE).result().isPresent) {
                baseDynamic = baseDynamic.remove(DataKeys.POKEMON_STATE)
            } else if (!state.get(DataKeys.POKEMON_STATE_SHOULDER).result().isPresent) {
                baseDynamic = baseDynamic.remove(DataKeys.POKEMON_STATE)
            } else if (!state.get(DataKeys.POKEMON_STATE_PLAYER_UUID).result().isPresent) {
                baseDynamic = baseDynamic.remove(DataKeys.POKEMON_STATE)
            } else if (!state.get(DataKeys.POKEMON_STATE_ID).result().isPresent) {
                baseDynamic = baseDynamic.remove(DataKeys.POKEMON_STATE)
            } else if (!state.get(DataKeys.POKEMON_STATE_POKEMON_UUID).result().isPresent) {
                baseDynamic = baseDynamic.remove(DataKeys.POKEMON_STATE)
            }
        }

        baseDynamic.get(DataKeys.POKEMON_MOVESET).result().ifPresent { moveSet ->
            val moves = mutableListOf<Dynamic<*>>()
            for (i in 0 until 4) {
                moveSet.get(DataKeys.POKEMON_MOVESET + i).result().ifPresent { move ->
                    moves.add(move)
                }
            }
            if (moves.isNotEmpty()) {
                baseDynamic = baseDynamic.remove(DataKeys.POKEMON_MOVESET)
                baseDynamic = baseDynamic.set(DataKeys.POKEMON_MOVESET, baseDynamic.createList(moves.stream()))
            }
        }

        return baseDynamic
    }

}