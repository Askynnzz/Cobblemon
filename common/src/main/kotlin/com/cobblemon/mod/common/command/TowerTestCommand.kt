/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.battlefactory.BattleFactoryTowerManager
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

/**
 * Test command for simulating Battle Factory Tower runs.
 * 
 * Usage:
 * - /towersimulate <difficulty> complete - Simulates full 7-arena completion
 * - /towersimulate <difficulty> fail - Simulates failure at arena 3
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
object TowerTestCommand {
    
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("towersimulate")
                .requires { it.hasPermission(2) }
                .then(
                    Commands.argument("difficulty", StringArgumentType.string())
                        .suggests { _, builder ->
                            builder.suggest("easy")
                            builder.suggest("normal")
                            builder.suggest("hard")
                            builder.buildFuture()
                        }
                        .then(
                            Commands.argument("outcome", StringArgumentType.string())
                                .suggests { _, builder ->
                                    builder.suggest("complete")
                                    builder.suggest("fail")
                                    builder.buildFuture()
                                }
                                .executes(::execute)
                        )
                )
        )
    }
    
    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val difficultyId = StringArgumentType.getString(context, "difficulty")
        val outcome = StringArgumentType.getString(context, "outcome")
        
        val config = BattleFactoryTowerManager.getConfig()
        val difficulty = config.getDifficulty(difficultyId)
        
        if (difficulty == null) {
            player.sendSystemMessage(Component.literal("§cInvalid difficulty: $difficultyId"))
            return 0
        }
        
        // Start tower (auto-selects first 3 Pokemon)
        BattleFactoryTowerManager.startTower(player, difficulty)
        
        player.sendSystemMessage(Component.literal("§e§lSimulating ${difficulty.displayName}§r"))
        
        when (outcome) {
            "complete" -> {
                player.sendSystemMessage(Component.literal("§aSimulating 7 victories..."))
                repeat(7) {
                    BattleFactoryTowerManager.onArenaVictory(player)
                }
            }
            "fail" -> {
                player.sendSystemMessage(Component.literal("§cSimulating 2 victories then failure..."))
                repeat(2) {
                    BattleFactoryTowerManager.onArenaVictory(player)
                }
                BattleFactoryTowerManager.onArenaDefeat(player)
            }
            else -> {
                player.sendSystemMessage(Component.literal("§cInvalid outcome: $outcome"))
                return 0
            }
        }
        
        return 1
    }
}
