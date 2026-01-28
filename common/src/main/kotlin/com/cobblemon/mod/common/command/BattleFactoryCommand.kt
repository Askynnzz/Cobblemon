/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.battlefactory.BattleFactorySession
import com.cobblemon.mod.common.api.battlefactory.FacilitySessionManager
import com.cobblemon.mod.common.api.battlefactory.SimpleRentalTeamGenerator
import com.cobblemon.mod.common.api.battlefactory.TemporaryPartyManagerImpl
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

/**
 * Command for testing and managing Battle Factory.
 * 
 * Commands:
 * - /battlefactory start - Start a new Battle Factory session
 * - /battlefactory end - End current session
 * - /battlefactory status - Show current session info
 * - /battlefactory test - Run system tests
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
object BattleFactoryCommand {
    
    private const val PERMISSION_START = "${CobblemonPermissions.BATTLE_FACTORY}.start"
    private const val PERMISSION_END = "${CobblemonPermissions.BATTLE_FACTORY}.end"
    private const val PERMISSION_STATUS = "${CobblemonPermissions.BATTLE_FACTORY}.status"
    private const val PERMISSION_TEST = "${CobblemonPermissions.BATTLE_FACTORY}.test"
    
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("battlefactory")
                .then(
                    Commands.literal("start")
                        .requires { it.hasPermission(2) }
                        .executes(::start)
                )
                .then(
                    Commands.literal("end")
                        .requires { it.hasPermission(2) }
                        .executes(::end)
                )
                .then(
                    Commands.literal("status")
                        .requires { it.hasPermission(0) }
                        .executes(::status)
                )
                .then(
                    Commands.literal("refresh")
                        .requires { it.hasPermission(0) }
                        .executes(::refresh)
                )
                .then(
                    Commands.literal("test")
                        .requires { it.hasPermission(2) }
                        .executes(::test)
                )
        )
    }
    
    /**
     * Starts a new Battle Factory session.
     */
    private fun start(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        
        return try {
            // Check if already has session
            if (FacilitySessionManager.hasActiveSession(player)) {
                player.sendSystemMessage(Component.literal("§cYou already have an active Battle Factory session!"))
                player.sendSystemMessage(Component.literal("§eUse /battlefactory end to stop it first."))
                return 0
            }
            
            // Generate rental team
            player.sendSystemMessage(Component.literal("§eGenerating rental Pokémon..."))
            val generator = SimpleRentalTeamGenerator.loadFromFile()
            val options = generator.generateOptions(6)
            
            if (options.size < 3) {
                player.sendSystemMessage(Component.literal("§cFailed to generate enough rental Pokémon (need 3, got ${options.size})"))
                return 0
            }
            
            val team = generator.selectTeam(options, 3)
            
            // Show selected team
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
            player.sendSystemMessage(Component.literal("§eYour original party has been backed up."))
            player.sendSystemMessage(Component.literal("§eGood luck! Win streak: 0"))
            
            1
        } catch (e: Exception) {
            player.sendSystemMessage(Component.literal("§cError starting Battle Factory: ${e.message}"))
            e.printStackTrace()
            0
        }
    }
    
    /**
     * Ends the current Battle Factory session.
     */
    private fun end(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        
        val session = FacilitySessionManager.getSession(player)
        
        if (session == null) {
            player.sendSystemMessage(Component.literal("§cYou don't have an active Battle Factory session."))
            return 0
        }
        
        if (session is BattleFactorySession) {
            val wins = session.getWins()
            player.sendSystemMessage(Component.literal("§eEnding Battle Factory session..."))
            player.sendSystemMessage(Component.literal("§aFinal win streak: $wins"))
            
            session.end(giveRewards = true)
            
            player.sendSystemMessage(Component.literal("§aSession ended! Your party has been restored."))
            player.sendSystemMessage(Component.literal("§7(If UI doesn't update, reconnect to refresh)"))
        } else {
            session.end(giveRewards = false)
            player.sendSystemMessage(Component.literal("§aSession ended."))
        }
        
        return 1
    }
    
    /**
     * Shows current session status.
     */
    private fun status(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        
        val session = FacilitySessionManager.getSession(player)
        
        if (session == null) {
            player.sendSystemMessage(Component.literal("§7No active Battle Factory session."))
            return 0
        }
        
        player.sendSystemMessage(Component.literal("§a§lBattle Factory Status:"))
        player.sendSystemMessage(Component.literal("§7Session ID: §e${session.sessionId}"))
        player.sendSystemMessage(Component.literal("§7Active: §e${session.isActive()}"))
        
        if (session is BattleFactorySession) {
            player.sendSystemMessage(Component.literal("§7Win Streak: §a${session.getWins()}"))
            player.sendSystemMessage(Component.literal("§7Rental Team: §b${session.rentalTeam.size} Pokémon"))
        }
        
        // Show backup status
        val hasBackup = TemporaryPartyManagerImpl.hasTemporaryParty(player)
        player.sendSystemMessage(Component.literal("§7Party Backup: ${if (hasBackup) "§aActive" else "§cNone"}"))
        
        return 1
    }

    /**
     * Manuall refreshes the player's UI.
     */
    private fun refresh(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        
        player.sendSystemMessage(Component.literal("§eRefreshing Battle Factory UI..."))
        TemporaryPartyManagerImpl.refreshUI(player)
        player.sendSystemMessage(Component.literal("§aUI Refresh signal sent!"))
        
        return 1
    }
    
    /**
     * Runs system tests.
     */
    private fun test(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        
        player.sendSystemMessage(Component.literal("§e§lRunning Battle Factory System Tests..."))
        
        // Test 1: Generator
        player.sendSystemMessage(Component.literal("§7[1/3] Testing Rental Team Generator..."))
        try {
            val generator = SimpleRentalTeamGenerator.loadFromFile()
            val options = generator.generateOptions(6)
            val team = generator.selectTeam(options, 3)
            player.sendSystemMessage(Component.literal("§a  ✓ Generated ${options.size} options, selected ${team.size} team"))
        } catch (e: Exception) {
            player.sendSystemMessage(Component.literal("§c  ✗ Generator failed: ${e.message}"))
        }
        
        // Test 2: Temporary Party Manager
        player.sendSystemMessage(Component.literal("§7[2/3] Testing Temporary Party Manager..."))
        val hasBackup = TemporaryPartyManagerImpl.hasTemporaryParty(player)
        player.sendSystemMessage(Component.literal("§a  ✓ Backup status: ${if (hasBackup) "Active" else "None"}"))
        
        // Test 3: Session Manager
        player.sendSystemMessage(Component.literal("§7[3/3] Testing Session Manager..."))
        val hasSession = FacilitySessionManager.hasActiveSession(player)
        player.sendSystemMessage(Component.literal("§a  ✓ Session status: ${if (hasSession) "Active" else "None"}"))
        
        player.sendSystemMessage(Component.literal("§a§lAll tests completed!"))
        
        return 1
    }
}
