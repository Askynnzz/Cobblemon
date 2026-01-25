/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.dialogue.ActiveDialogue
import com.cobblemon.mod.common.api.dialogue.DialogueAction

/**
 * Dialogue action for starting a Battle Factory Tower session.
 * 
 * Triggered when player selects a difficulty level in the lobby NPC dialogue.
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
class TowerStartDialogueAction : DialogueAction {
    
    companion object {
        const val TYPE = "tower_start"
    }
    
    override fun invoke(dialogue: ActiveDialogue, input: String?) {
        val player = dialogue.playerEntity
        
        // Get selected difficulty from input parameter
        val difficultyId = input ?: run {
            Cobblemon.LOGGER.warn("No difficulty selected in tower dialogue")
            return
        }
        
        // Handle exit
        if (difficultyId == "exit") {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7Come back when you're ready!"))
            return
        }
        
        // Get difficulty config
        val config = BattleFactoryTowerManager.getConfig()
        val difficulty = config.getDifficulty(difficultyId)
        
        if (difficulty == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cInvalid difficulty: $difficultyId"))
            Cobblemon.LOGGER.error("Unknown tower difficulty: $difficultyId")
            return
        }
        
        // Start tower session
        BattleFactoryTowerManager.startTower(player, difficulty)
    }
}
