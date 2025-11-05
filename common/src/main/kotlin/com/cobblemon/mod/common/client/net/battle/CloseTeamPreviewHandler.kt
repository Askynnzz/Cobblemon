package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.battle.preview.TeamPreviewGUI
import com.cobblemon.mod.common.net.messages.client.battle.CloseTeamPreviewPacket
import net.minecraft.client.Minecraft

object CloseTeamPreviewHandler : ClientNetworkPacketHandler<CloseTeamPreviewPacket> {
    override fun handle(packet: CloseTeamPreviewPacket, client: Minecraft) {
        if (client.screen is TeamPreviewGUI) {
            client.screen!!.onClose()
        }
    }
}