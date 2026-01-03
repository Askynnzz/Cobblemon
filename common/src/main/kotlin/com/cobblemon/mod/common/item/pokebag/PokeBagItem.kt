/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.pokebag

import com.cobblemon.mod.common.item.pokebag.inventory.PokeBagMenuProvider
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.Level

class PokeBagItem(val size: Int, rarity: Rarity) : Item(Properties().rarity(rarity).stacksTo(1)) {

    fun open(user: ServerPlayer, stack: ItemStack) {
        user.openMenu(PokeBagMenuProvider(stack))
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?>? {
        val stack = user.getItemInHand(hand)

        if (user is ServerPlayer) {
            open(user, stack)
        }
        return InteractionResultHolder.success(stack)
    }

    override fun canFitInsideContainerItems(): Boolean {
        return false
    }

}