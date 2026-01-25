/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import com.cobblemon.mod.common.Cobblemon
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/**
 * Distributes rewards for Battle Factory Tower completion.
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
object TowerRewardDistributor {
    
    /**
     * Gives completion rewards based on difficulty.
     */
    fun giveCompletionRewards(player: ServerPlayer, difficulty: TowerDifficulty) {
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6§l★ REWARDS ★"))
        
        difficulty.completionReward.items.forEach { itemString ->
            giveItem(player, itemString)
        }
        
        Cobblemon.LOGGER.info("Gave completion rewards to ${player.name.string} for ${difficulty.id}")
    }
    
    /**
     * Gives progress rewards every 2 arenas.
     */
    fun giveProgressReward(player: ServerPlayer, arenaNumber: Int, difficulty: TowerDifficulty) {
        if (arenaNumber % 2 == 0 && arenaNumber > 0) {
            val candyCount = (arenaNumber / 2 * difficulty.rewardMultiplier).toInt()
            if (candyCount > 0) {
                giveItem(player, "cobblemon:rare_candy:$candyCount")
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§a+$candyCount Rare Candy (Progress Reward)")
                )
            }
        }
    }
    
    /**
     * Parses and gives an item string like "cobblemon:rare_candy:3".
     */
    private fun giveItem(player: ServerPlayer, itemString: String) {
        try {
            val parts = itemString.split(":")
            val count = parts.getOrNull(2)?.toIntOrNull() ?: 1
            val itemId = parts.take(2).joinToString(":")
            
            val item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId))
            val stack = ItemStack(item, count)
            
            if (!stack.isEmpty) {
                player.inventory.add(stack)
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§a+${stack.count} ${stack.hoverName.string}")
                )
            }
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Failed to give item: $itemString", e)
        }
    }
}
