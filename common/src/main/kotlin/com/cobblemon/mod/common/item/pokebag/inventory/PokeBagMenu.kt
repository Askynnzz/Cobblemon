/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.pokebag.inventory

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.item.pokebag.PokeBagItem
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class PokeBagMenu(id: Int, inventory: Inventory, val pokebag: ItemStack) : AbstractContainerMenu(getMenuType(pokebag), id) {

    val container = PokeBagContainer(getPokeBagSize(), pokebag)

    init {
        container.startOpen(inventory.player)

        val i = (getPokeBagRows() - 4) * 18

        for (j in 0 until getPokeBagRows()) {
            for (k in 0 until 9) {
                addSlot(PokeBagSlot(container, k + j * 9, 8 + k * 18, 18 + j * 18))
            }
        }

        for (j in 0 until 3) {
            for (k in 0 until 9) {
                addSlot(Slot(inventory, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i))
            }
        }

        for (j in 0 until 9) {
            addSlot(Slot(inventory, j, 8 + j * 18, 161 + i))
        }
    }

    fun getPokeBagSize(): Int {
        return getPokeBagRows() * 9
    }

    fun getPokeBagRows(): Int {
        val item = pokebag.item as PokeBagItem
        return item.size
    }

    override fun quickMoveStack(
        player: Player?,
        index: Int
    ): ItemStack? {
        var stack = ItemStack.EMPTY
        val slot = slots[index]
        if (slot != null && slot.hasItem()) {
            val stack2 = slot.item
            stack = stack2.copy()
            if (index < container.containerSize) {
                if (!this.moveItemStackTo(stack2, container.containerSize, slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!this.moveItemStackTo(stack2, 0, container.containerSize, false)) {
                return ItemStack.EMPTY
            }

            if (stack2.isEmpty) {
                slot.setByPlayer(ItemStack.EMPTY)
            }
            else {
                slot.setChanged()
            }
        }

        return stack
    }

    override fun clicked(slotId: Int, button: Int, clickType: ClickType?, player: Player?) {
        if (slotId < 0 || slotId >= this.slots.size) {
            super.clicked(slotId, button, clickType, player)
            return
        }

        val stack = slots[slotId]
        if (stack != pokebag) {
            super.clicked(slotId, button, clickType, player)
        }
    }

    override fun stillValid(player: Player): Boolean {
        return container.stillValid(player)
    }

    override fun removed(player: Player?) {
        super.removed(player)
        this.container.stopOpen(player)
    }

    companion object {
        fun getMenuType(backpack: ItemStack): MenuType<*> {
            val item = backpack.item as PokeBagItem
            return when (item.size) {
                3 -> PokeBagMenuType.SMALL_POKE_BAG
                4 -> PokeBagMenuType.MEDIUM_POKE_BAG
                5 -> PokeBagMenuType.LARGE_POKE_BAG
                6 -> PokeBagMenuType.HUGE_POKE_BAG
                else -> PokeBagMenuType.SMALL_POKE_BAG
            }
        }

        fun small(id: Int, inventory: Inventory): PokeBagMenu {
            return PokeBagMenu(id, inventory, ItemStack(CobblemonItems.SMALL_POKE_BAG))
        }

        fun medium(id: Int, inventory: Inventory): PokeBagMenu {
            return PokeBagMenu(id, inventory, ItemStack(CobblemonItems.MEDIUM_POKE_BAG))
        }

        fun large(id: Int, inventory: Inventory): PokeBagMenu {
            return PokeBagMenu(id, inventory, ItemStack(CobblemonItems.LARGE_POKE_BAG))
        }

        fun huge(id: Int, inventory: Inventory): PokeBagMenu {
            return PokeBagMenu(id, inventory, ItemStack(CobblemonItems.HUGE_POKE_BAG))
        }
    }
}