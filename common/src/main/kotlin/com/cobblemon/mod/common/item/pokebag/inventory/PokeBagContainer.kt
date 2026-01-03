/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.pokebag.inventory

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokebag.PokeBagContainerValidCheckEvent
import net.minecraft.core.NonNullList
import net.minecraft.core.component.DataComponents
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemContainerContents

class PokeBagContainer(val size: Int, val backpack: ItemStack) : Container {

    val items = NonNullList.withSize(size, ItemStack.EMPTY)

    init {
        backpack.get(DataComponents.CONTAINER)?.copyInto(items)
    }

    override fun getContainerSize(): Int {
        return size
    }

    override fun isEmpty(): Boolean {
        return items.isEmpty()
    }

    override fun getItem(slot: Int): ItemStack {
        return items[slot]
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack? {
        val stack = ContainerHelper.removeItem(items, slot, amount)
        setChanged()
        return stack
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack? {
        val stack = ContainerHelper.takeItem(items, slot)
        setChanged()
        return stack
    }

    override fun setItem(slot: Int, stack: ItemStack?) {
        items.set(slot, stack)
        setChanged()
    }

    override fun setChanged() {
        backpack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items))
    }

    override fun stillValid(player: Player): Boolean {
        if (player.inventory.contains(backpack)) return true

        val event = PokeBagContainerValidCheckEvent(backpack, player, false)
        CobblemonEvents.POKE_BAG_CONTAINER_VALID_CHECK.emit(event)
        return event.isValid
    }

    override fun clearContent() {
        items.clear()
        setChanged()
    }

}