package com.cobblemon.mod.common.api.events.pokemon

import com.cobblemon.mod.common.pokemon.Pokemon

data class PokemonHealthChangeEvent(
    val pokemon: Pokemon,
    val old: Int,
    val new: Int,
    val delta: Int
)