package com.cobblemon.mod.common.net.serverhandling.battle

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.battles.TeamManager
import com.cobblemon.mod.common.battles.TeamManager.TeamRequest
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.battle.TeamPreviewSelectPokemonPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object TeamPreviewSelectPokemonHandler : ServerNetworkPacketHandler<TeamPreviewSelectPokemonPacket> {
    override fun handle(packet: TeamPreviewSelectPokemonPacket, server: MinecraftServer, player: ServerPlayer) {
        TODO()
    }
}