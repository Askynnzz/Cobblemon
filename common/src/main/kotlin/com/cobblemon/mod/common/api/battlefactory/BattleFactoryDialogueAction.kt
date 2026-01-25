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
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.util.party
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

/**
 * Dialogue action for Battle Factory NPC interactions.
 * 
 * Handles player choices from the Battle Factory dialogue:
 * - "start" → Start new session
 * - "end" → End current session
 * - "status" → Show session info  
 * - "exit" → Close dialogue
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
class BattleFactoryDialogueAction : DialogueAction {
    
    override fun invoke(dialogue: ActiveDialogue, input: String?) {
        val player = dialogue.playerEntity
        val chosenOption = input ?: return
        
        when (chosenOption) {
            "start" -> handleStart(player, dialogue)
            "end" -> handleEnd(player, dialogue)
            "status" -> handleStatus(player, dialogue)
            "exit" -> {
                player.sendSystemMessage(Component.literal("§7Come back anytime!"))
            }
            else -> {
                player.sendSystemMessage(Component.literal("§cInvalid option."))
            }
        }
    }
    
    private fun handleStart(player: ServerPlayer, dialogue: ActiveDialogue) {
        try {
            // Check if already has session
            if (FacilitySessionManager.hasActiveSession(player)) {
                player.sendSystemMessage(Component.literal("§cYou already have an active session!"))
                player.sendSystemMessage(Component.literal("§eEnd it first with the 'End Session' option."))
                return
            }
            
            // Check if has at least 1 Pokémon to backup
            if (player.party().count() == 0) {
                player.sendSystemMessage(Component.literal("§cYou need at least 1 Pokémon to participate!"))
                return
            }
            
            // Generate rental team
            player.sendSystemMessage(Component.literal("§eGenerating your rental Pokémon..."))
            val generator = SimpleRentalTeamGenerator.loadFromFile()
            val options = generator.generateOptions(6)
            
            if (options.size < 3) {
                player.sendSystemMessage(Component.literal("§cFailed to generate rental team. Please try again."))
                Cobblemon.LOGGER.error("Failed to generate enough rental Pokémon for ${player.name.string}")
                return
            }
            
            val team = generator.selectTeam(options, 3)
            
            // Show rental team
            player.sendSystemMessage(Component.literal("§a§lYour Rental Team:"))
            team.forEachIndexed { index, pokemon ->
                player.sendSystemMessage(
                    Component.literal("§7${index + 1}. §b${pokemon.species.translatedName.string} §7Lv${pokemon.level}")
                )
            }
            
            // Create and start session
            val session = BattleFactorySession(player.uuid, team)
            FacilitySessionManager.startSession(player, session)
            
            player.sendSystemMessage(Component.literal("§a§lBattle Factory session started!"))
            player.sendSystemMessage(Component.literal("§6Good luck! Your win streak: 0"))
            
        } catch (e: Exception) {
            player.sendSystemMessage(Component.literal("§cError starting session: ${e.message}"))
            Cobblemon.LOGGER.error("Error starting Battle Factory session", e)
        }
    }
    
    private fun handleEnd(player: ServerPlayer, dialogue: ActiveDialogue) {
        val session = FacilitySessionManager.getSession(player)
        
        if (session == null) {
            player.sendSystemMessage(Component.literal("§cYou don't have an active session."))
            return
        }
        
        if (session is BattleFactorySession) {
            val wins = session.getWins()
            player.sendSystemMessage(Component.literal("§eEnding your Battle Factory session..."))
            player.sendSystemMessage(Component.literal("§aFinal win streak: §6$wins"))
            
            session.end(giveRewards = true)
            
            player.sendSystemMessage(Component.literal("§aSession ended! Your party has been restored."))
            if (wins > 0) {
                player.sendSystemMessage(Component.literal("§6You've received rewards for your performance!"))
            }
        } else {
            session.end(giveRewards = false)
        }
    }
    
    private fun handleStatus(player: ServerPlayer, dialogue: ActiveDialogue) {
        val session = FacilitySessionManager.getSession(player)
        
        if (session == null) {
            player.sendSystemMessage(Component.literal("§7No active Battle Factory session."))
            player.sendSystemMessage(Component.literal("§eStart one with the 'Start Battle Factory' option!"))
            return
        }
        
        player.sendSystemMessage(Component.literal("§a§lBattle Factory Status:"))
        player.sendSystemMessage(Component.literal("§7Session: §e${session.isActive()}"))
        
        if (session is BattleFactorySession) {
            val wins = session.getWins()
            player.sendSystemMessage(Component.literal("§7Win Streak: §a$wins"))
            player.sendSystemMessage(Component.literal("§7Rental Team: §b${session.rentalTeam.size} Pokémon"))
            
            // Show next milestone
            val nextMilestone = when {
                wins < 7 -> 7
                wins < 14 -> 14
                wins < 21 -> 21
                else -> null
            }
            
            if (nextMilestone != null) {
                player.sendSystemMessage(Component.literal("§7Next reward at: §6$nextMilestone wins"))
            } else {
                player.sendSystemMessage(Component.literal("§6You've reached all milestones! Keep going!"))
            }
        }
        
        val hasBackup = TemporaryPartyManagerImpl.hasTemporaryParty(player)
        player.sendSystemMessage(Component.literal("§7Party Backup: ${if (hasBackup) "§aActive" else "§cNone"}"))
    }
    
    companion object {
        const val TYPE = "battlefactory_action"
    }
}
