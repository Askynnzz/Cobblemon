/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.SingleActionRequest
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket
import net.minecraft.client.Minecraft

object BattleQueueRequestHandler : ClientNetworkPacketHandler<BattleQueueRequestPacket> {
    override fun handle(packet: BattleQueueRequestPacket, client: Minecraft) {
        Cobblemon.LOGGER.info("[TOWER DEBUG CLIENT] BattleQueueRequestPacket received")
        Cobblemon.LOGGER.info("[TOWER DEBUG CLIENT] Request content: ${packet.request}")
        Cobblemon.LOGGER.info("[TOWER DEBUG CLIENT] Request.side is ${if (packet.request.side != null) "NOT NULL" else "NULL"}")
        
        if (packet.request.side != null) {
            val side = packet.request.side!!
            Cobblemon.LOGGER.info("[TOWER DEBUG CLIENT] Side info: name=${side.name}, id=${side.id}, pokemon count=${side.pokemon.size}")
            side.pokemon.forEachIndexed { index, pokemon ->
                Cobblemon.LOGGER.info("[TOWER DEBUG CLIENT]   Pokemon[$index]: uuid=${pokemon.uuid}, active=${pokemon.active}, commanding=${pokemon.commanding}, condition=${pokemon.condition}")
            }
        }
        
        val battle = CobblemonClient.battle ?: run {
            Cobblemon.LOGGER.warn("[TOWER DEBUG CLIENT] Battle is NULL, cannot process request")
            return
        }
        
        val actor = battle.side1.actors.find { it.uuid == Minecraft.getInstance().player?.uuid } ?: run {
            Cobblemon.LOGGER.warn("[TOWER DEBUG CLIENT] Actor not found for player ${Minecraft.getInstance().player?.uuid}")
            return
        }
        
        Cobblemon.LOGGER.info("[TOWER DEBUG CLIENT] Actor found: ${actor.uuid}, active pokemon count: ${actor.activePokemon.size}")
        actor.activePokemon.forEachIndexed { index, activePokemon ->
            Cobblemon.LOGGER.info("[TOWER DEBUG CLIENT]   ActivePokemon[$index]: battlePokemon.uuid=${activePokemon.battlePokemon?.uuid}")
        }
        
        val requests = SingleActionRequest.composeFrom(actor, packet.request)
        Cobblemon.LOGGER.info("[TOWER DEBUG CLIENT] Created ${requests.size} SingleActionRequest(s)")
        requests.forEachIndexed { index, request ->
            Cobblemon.LOGGER.info("[TOWER DEBUG CLIENT]   SingleActionRequest[$index]: side=${if (request.side != null) "NOT NULL" else "NULL"}, moveSet=${if (request.moveSet != null) "NOT NULL" else "NULL"}, forceSwitch=${request.forceSwitch}")
        }
        
        CobblemonClient.battle?.pendingActionRequests = requests
        Cobblemon.LOGGER.info("[TOWER DEBUG CLIENT] Pending action requests set successfully")
    }
}