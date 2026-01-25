/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Generator for rental Pokémon teams used in Battle Factory.
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
interface RentalTeamGenerator {
    
    /**
     * Generates a list of rental Pokémon options for the player to choose from.
     * 
     * @param count Number of options to generate (default: 6)
     * @return List of Pokemon that can be selected
     */
    fun generateOptions(count: Int = 6): List<Pokemon>
    
    /**
     * Selects a team from the generated options.
     * 
     * In V1, this will select randomly server-side.
     * In V2, this can be extended to send options to client for manual selection.
     * 
     * @param options The list of Pokemon to choose from
     * @param count Number of Pokemon to select (default: 3)
     * @return The selected team
     */
    fun selectTeam(options: List<Pokemon>, count: Int = 3): List<Pokemon>
}
