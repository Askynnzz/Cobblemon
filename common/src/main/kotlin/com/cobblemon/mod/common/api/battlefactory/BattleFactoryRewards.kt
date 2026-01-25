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
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

/**
 * Handles reward distribution for Battle Factory based on win streaks.
 * 
 * Reward tiers:
 * - 7 wins: Basic rewards (Rare Candies, etc.)
 * - 14 wins: Intermediate rewards (TMs, held items, etc.)
 * - 21 wins: Advanced rewards (Master Ball, Ability Capsule, etc.)
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
object BattleFactoryRewards {
    
    /**
     * Distributes rewards to a player based on their win count.
     * 
     * @param player The player to give rewards to
     * @param wins The number of wins achieved
     */
    fun giveRewards(player: ServerPlayer, wins: Int) {
        val rewards = mutableListOf<ItemStack>()
        
        // Tier 1: 7+ wins
        if (wins >= 7) {
            rewards.add(ItemStack(com.cobblemon.mod.common.CobblemonItems.RARE_CANDY, 3))
            rewards.add(ItemStack(Items.GOLD_INGOT, 5))
        }
        
        // Tier 2: 14+ wins
        if (wins >= 14) {
            rewards.add(ItemStack(com.cobblemon.mod.common.CobblemonItems.MASTER_BALL, 1))
            rewards.add(ItemStack(Items.DIAMOND, 5))
        }
        
        // Tier 3: 21+ wins
        if (wins >= 21) {
            rewards.add(ItemStack(com.cobblemon.mod.common.CobblemonItems.ABILITY_CAPSULE, 1))
            rewards.add(ItemStack(Items.NETHERITE_INGOT, 2))
        }
        
        // Give rewards to player
        rewards.forEach { reward ->
            if (!player.inventory.add(reward)) {
                // If inventory full, drop at player's feet
                player.drop(reward, false)
            }
        }
        
        if (rewards.isNotEmpty()) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("§6You received rewards for your ${wins} win streak!")
            )
            Cobblemon.LOGGER.info("Gave Battle Factory rewards to ${player.name.string} for $wins wins")
        }
    }
}
