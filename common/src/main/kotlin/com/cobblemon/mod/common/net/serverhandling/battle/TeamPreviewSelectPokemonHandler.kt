package com.cobblemon.mod.common.net.serverhandling.battle

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.battles.TeamPreviewManager
import com.cobblemon.mod.common.net.messages.server.battle.TeamPreviewSelectPokemonPacket
import com.cobblemon.mod.common.util.safeParty
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object TeamPreviewSelectPokemonHandler : ServerNetworkPacketHandler<TeamPreviewSelectPokemonPacket> {
    override fun handle(packet: TeamPreviewSelectPokemonPacket, server: MinecraftServer, player: ServerPlayer) {
        val party = player.safeParty()?.toGappyList() ?: return
        for (slot in packet.selected) {
            if (slot < 0 || slot >= party.size) return
            if (party[slot] == null) return
        }
        TeamPreviewManager.changeSelections(player, packet.selected)
    }
}