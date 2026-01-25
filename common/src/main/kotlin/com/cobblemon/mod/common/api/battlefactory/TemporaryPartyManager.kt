/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer

/**
 * Manager interface for safely swapping a player's party with a temporary rental team.
 * 
 * This system ensures that:
 * - The player's original party is always backed up before any swap
 * - Backups are persisted to survive crashes and disconnections
 * - Restoration happens automatically in all critical scenarios (logout, death, crash)
 * - No Pokémon can ever be lost due to failures
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
interface TemporaryPartyManager {
    
    /**
     * Saves the player's current party as a backup before applying a rental team.
     * 
     * This creates a persistent backup in both memory and on disk.
     * If a backup already exists for this player, this will fail to prevent data corruption.
     * 
     * @param player The player whose party should be backed up
     * @return true if backup was successful, false if a backup already exists or failed
     */
    fun saveOriginal(player: ServerPlayer): Boolean
    
    /**
     * Applies a temporary rental party to the player, completely replacing their current party.
     * 
     * This should only be called AFTER [saveOriginal] has been successfully executed.
     * The player's party will be cleared and replaced with the rental Pokémon.
     * 
     * @param player The player to apply the rental team to
     * @param rentalTeam The list of rental Pokémon to give the player
     * @return true if the swap was successful, false if no backup exists or failed
     */
    fun applyTemporary(player: ServerPlayer, rentalTeam: List<Pokemon>): Boolean
    
    /**
     * Restores the player's original party from backup.
     * 
     * This clears the current party and restores all Pokémon from the backup.
     * After restoration, the backup data is cleaned up.
     * 
     * @param player The player whose party should be restored
     * @param force If true, will attempt restore even if session seems invalid
     * @return true if restoration was successful, false if no backup found or failed
     */
    fun restore(player: ServerPlayer, force: Boolean = false): Boolean
    
    /**
     * Checks if the player currently has an active temporary party session.
     * 
     * @param player The player to check
     * @return true if the player has a backup and is using a rental team
     */
    fun hasTemporaryParty(player: ServerPlayer): Boolean
    
    /**
     * Retrieves the current session data for a player, if any exists.
     * 
     * @param player The player whose session to retrieve
     * @return The TemporaryPartyData if one exists, null otherwise
     */
    fun getSessionData(player: ServerPlayer): TemporaryPartyData?
    
    /**
     * Updates the win count for a player's current session.
     * 
     * @param player The player whose wins to update
     * @param wins The new win count
     */
    fun updateWins(player: ServerPlayer, wins: Int)
}
