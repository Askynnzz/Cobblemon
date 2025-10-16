package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.battle.ClientBattleInformationRepository
import com.cobblemon.mod.common.net.messages.client.battle.BattleTimerPacket
import net.minecraft.client.Minecraft

object BattleTimerHandler : ClientNetworkPacketHandler<BattleTimerPacket> {
    override fun handle(packet: BattleTimerPacket, client: Minecraft) {
        ClientBattleInformationRepository.mustChooseBy = packet.mustChooseBy
    }
}