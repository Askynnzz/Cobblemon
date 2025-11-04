package com.cobblemon.mod.common.net.messages.server.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class TeamPreviewSelectPokemonPacket(val selected: Set<UUID>) : NetworkPacket<TeamPreviewSelectPokemonPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(selected) { _, value -> buffer.writeUUID(value) }
    }

    companion object {
        val ID = cobblemonResource("team_preview_select_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf) = TeamPreviewSelectPokemonPacket(
            buffer.readList { buffer.readUUID() }.toSet()
        )
    }
}