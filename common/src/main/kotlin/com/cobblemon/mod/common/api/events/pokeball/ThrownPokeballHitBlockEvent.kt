/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokeball

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.phys.BlockHitResult

/**
 * Event fired when a thrown Pokeball hits a Pokémon. Cancelling this event prevents the capture being started.
 */
class ThrownPokeballHitBlockEvent(
    val pokeBall : EmptyPokeBallEntity,
    val hitResult : BlockHitResult
) : Cancelable() {
    val context = mutableMapOf(
        "pokeball" to pokeBall.struct
    )
}