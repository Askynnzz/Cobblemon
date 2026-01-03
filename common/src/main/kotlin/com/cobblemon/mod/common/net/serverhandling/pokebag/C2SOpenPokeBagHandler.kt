/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.pokebag

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokebag.PokeBagOpenRequestEvent
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.pokebag.C2SOpenPokeBagPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object C2SOpenPokeBagHandler : ServerNetworkPacketHandler<C2SOpenPokeBagPacket> {
    override fun handle(packet: C2SOpenPokeBagPacket, server: MinecraftServer, player: ServerPlayer) {
        server.executeIfPossible {
            CobblemonEvents.POKE_BAG_OPEN_REQUEST.emit(PokeBagOpenRequestEvent(player))
        }
    }
}