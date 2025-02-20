/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.evolution.PreEvolution
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace

// We use this to "lazy" load a pre evolution since we can't validate all forms and species during species loading
internal class CobblemonLazyPreEvolution(rawData: String) : PreEvolution {

    val data: String

    init {
        if (" " in rawData) {
            val split = rawData.split(" ")
            if (split.size > 2) {
                Cobblemon.LOGGER.info("PreEvolution $rawData has more than 2 spaces")
            }

            if (split[1].startsWith("form=")) {
                this.data = "${split[0]} ${split[1]}"
            } else {
                this.data = "${split[0]} form=${split[1]}"
            }
        } else {
            this.data = rawData
        }
    }

    private val properties: PokemonProperties
        get() = PokemonProperties.parse(this.data)

    private val lazySpecies: Species by lazy {
        this.properties.species?.asIdentifierDefaultingNamespace()?.let { PokemonSpecies.getByIdentifier(it) } ?: throw IllegalArgumentException("A PreEvolution needs a valid species")
    }

    private val lazyForm: FormData by lazy {
        return@lazy this.properties.form?.let { formId -> this.species.forms.firstOrNull { it.aspects.contains(formId) } } ?: this.species.standardForm
    }

    override val species: Species
        get() = this.lazySpecies
    override val form: FormData
        get() = this.lazyForm
}