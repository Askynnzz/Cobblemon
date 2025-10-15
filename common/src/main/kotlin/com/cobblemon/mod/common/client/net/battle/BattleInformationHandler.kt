package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.battle.ClientBattleInformationRepository
import com.cobblemon.mod.common.net.messages.client.battle.BattleInformationPacket
import net.minecraft.client.Minecraft

object BattleInformationHandler : ClientNetworkPacketHandler<BattleInformationPacket> {
    override fun handle(packet: BattleInformationPacket, client: Minecraft) {
        ClientBattleInformationRepository.battles[packet.battle] = packet.informationDTO
    }
}