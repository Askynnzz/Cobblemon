/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import java.util.UUID

/**
 * Interface for a Battle Facility session (Battle Factory, Battle Tower, etc.).
 * 
 * A session represents an ongoing streak of battles within a facility.
 * It tracks progression, wins/losses, and handles lifecycle events.
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
interface FacilitySession {
    
    /** UUID of the player participating in this session */
    val playerUUID: UUID
    
    /** Unique identifier for this specific session */
    val sessionId: UUID
    
    /**
     * Starts the session.
     * This should initialize the session state and prepare the player.
     */
    fun start()
    
    /**
     * Called when the player wins a battle in this session.
     */
    fun onWin()
    
    /**
     * Called when the player loses a battle in this session.
     */
    fun onLose()
    
    /**
     * Ends the session.
     * 
     * @param giveRewards If true, rewards will be distributed based on performance
     */
    fun end(giveRewards: Boolean = true)
    
    /**
     * Checks if this session is currently active.
     * 
     * @return true if the session is active, false otherwise
     */
    fun isActive(): Boolean
}
