package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class CloseTeamPreviewPacket : NetworkPacket<CloseTeamPreviewPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
    }

    companion object {
        val ID = cobblemonResource("close_team_preview")

        fun decode(buffer: RegistryFriendlyByteBuf): CloseTeamPreviewPacket {
            return CloseTeamPreviewPacket()
        }
    }
}