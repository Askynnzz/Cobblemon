package com.cobblemon.mod.common.net.messages.server.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class SpectateNearestBattlePacket : NetworkPacket<SpectateNearestBattlePacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {}

    companion object {
        val ID = cobblemonResource("battle_spectate_nearest")
        fun decode(buffer: RegistryFriendlyByteBuf) = SpectateNearestBattlePacket()
    }
}