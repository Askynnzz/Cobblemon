package com.cobblemon.mod.common.client.gui.battle.preview

import com.cobblemon.mod.common.client.battle.preview.ClientBattleTeamPreview
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.util.battleLang
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen

class TeamPreviewGUI(val preview: ClientBattleTeamPreview) : Screen(battleLang("gui.team_preview")), CobblemonRenderable {
    private lateinit var widget: TeamPreviewWidget

    override fun init() {
        super.init()
        widget = addRenderableWidget(TeamPreviewWidget(preview))
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun shouldCloseOnEsc() = false
}