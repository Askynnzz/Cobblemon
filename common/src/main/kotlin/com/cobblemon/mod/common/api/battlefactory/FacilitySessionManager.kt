/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import com.cobblemon.mod.common.Cobblemon
import net.minecraft.server.level.ServerPlayer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Global manager for all active facility sessions.
 * 
 * Ensures that:
 * - Only one session can be active per player at a time
 * - Sessions are properly tracked and cleaned up
 * - Cross-facility session management (for future Battle Tower, etc.)
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
object FacilitySessionManager {
    
    private val activeSessions = ConcurrentHashMap<UUID, FacilitySession>()
    
    /**
     * Starts a new facility session for a player.
     * 
     * @param player The player starting the session
     * @param session The session to start
     * @throws IllegalStateException if the player already has an active session
     */
    fun startSession(player: ServerPlayer, session: FacilitySession) {
        if (activeSessions.containsKey(player.uuid)) {
            val existingSession = activeSessions[player.uuid]
            throw IllegalStateException("Player ${player.name.string} already has an active ${existingSession!!.javaClass.simpleName}")
        }
        
        activeSessions[player.uuid] = session
        session.start()
        
        Cobblemon.LOGGER.info("Started ${session.javaClass.simpleName} for ${player.name.string}")
    }
    
    /**
     * Gets the active session for a player, if any.
     * 
     * @param player The player to check
     * @return The active session, or null if none exists
     */
    fun getSession(player: ServerPlayer): FacilitySession? {
        return activeSessions[player.uuid]
    }
    
    /**
     * Gets the active session for a player UUID, if any.
     * 
     * @param playerUUID The player UUID to check
     * @return The active session, or null if none exists
     */
    fun getSession(playerUUID: UUID): FacilitySession? {
        return activeSessions[playerUUID]
    }
    
    /**
     * Ends the session for a player.
     * 
     * @param playerUUID The UUID of the player whose session to end
     */
    fun endSession(playerUUID: UUID) {
        activeSessions.remove(playerUUID)
    }
    
    /**
     * Checks if a player has an active session.
     * 
     * @param player The player to check
     * @return true if the player has an active session
     */
    fun hasActiveSession(player: ServerPlayer): Boolean {
        return activeSessions.containsKey(player.uuid)
    }
    
    /**
     * Forces all active sessions to end.
     * Used during server shutdown to ensure all parties are restored.
     */
    fun endAllSessions() {
        Cobblemon.LOGGER.info("Ending all active facility sessions (${activeSessions.size} sessions)")
        
        activeSessions.values.forEach { session ->
            try {
                session.end(giveRewards = false)
            } catch (e: Exception) {
                Cobblemon.LOGGER.error("Error ending session ${session.sessionId}", e)
            }
        }
        
        activeSessions.clear()
    }
}
