package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.battle.ClientBattleInformationRepository
import com.cobblemon.mod.common.net.messages.client.battle.BattleActorTeamPacket
import net.minecraft.client.Minecraft

object BattleActorTeamHandler : ClientNetworkPacketHandler<BattleActorTeamPacket> {
    override fun handle(packet: BattleActorTeamPacket, client: Minecraft) {
        ClientBattleInformationRepository.actors[packet.actor] = packet.team.toMutableList()
    }
}