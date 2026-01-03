/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.megaevolution

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.gimmicks.megaevolution.MegaStoneUtils
import com.cobblemon.mod.common.net.messages.server.megaevolution.C2SRemoveMegaEvolutionPacket
import com.cobblemon.mod.common.util.party
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object C2SRemoveMegaEvolutionHandler : ServerNetworkPacketHandler<C2SRemoveMegaEvolutionPacket> {
    override fun handle(packet: C2SRemoveMegaEvolutionPacket, server: MinecraftServer, player: ServerPlayer) {
        val pokemonEntity = player.serverLevel().getEntity(packet.pokemon) as? PokemonEntity ?: return
        if (player.party().none { it == pokemonEntity.pokemon }) return
        MegaStoneUtils.removeMegaEvolution(pokemonEntity.pokemon)
    }
}