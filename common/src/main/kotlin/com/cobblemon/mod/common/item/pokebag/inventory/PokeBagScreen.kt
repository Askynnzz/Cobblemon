/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.pokebag.inventory

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.MenuAccess
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

class PokeBagScreen(val menu: PokeBagMenu, inventory: Inventory, title: Component) : AbstractContainerScreen<PokeBagMenu>(menu, inventory, title), MenuAccess<PokeBagMenu> {

    val containerRows: Int

    init {
        val i = 222
        val j = 114
        containerRows = menu.getPokeBagRows()
        this.imageHeight = 114 + containerRows * 18
        this.inventoryLabelY = this.imageHeight - 94
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val i = (this.width - this.imageWidth) / 2
        val j = (this.height - this.imageHeight) / 2
        guiGraphics.blit(CONTAINER_BACKGROUND, i, j, 0, 0, this.imageWidth, this.containerRows * 18 + 17)
        guiGraphics.blit(CONTAINER_BACKGROUND, i, j + containerRows * 18 + 17, 0, 126, this.imageWidth, 96)
    }

    companion object {
        val CONTAINER_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png")
    }
}