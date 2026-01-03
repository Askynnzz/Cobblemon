/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.megaevolution

import com.cobblemon.mod.common.MegaEvolutionEventHandler
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import com.cobblemon.mod.common.net.messages.server.megaevolution.C2SMegaEvolvePacket
import com.cobblemon.mod.common.util.party
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.time.Instant
import kotlin.collections.set

object C2SMegaEvolveHandler : ServerNetworkPacketHandler<C2SMegaEvolvePacket> {
    override fun handle(packet: C2SMegaEvolvePacket, server: MinecraftServer, player: ServerPlayer) {
        val pokemonEntity = player.serverLevel().getEntity(packet.pokemon) as? PokemonEntity ?: return
        if (player.party().none { it == pokemonEntity.pokemon }) return

        val position = pokemonEntity.position()
        val packet = SpawnSnowstormParticlePacket(
            ResourceLocation.parse("cobblemon:mega_evolution_particles"),
            pokemonEntity.position().add(0.0, pokemonEntity.boundingBox.ysize / 2, 0.0)
        )
        packet.sendToPlayersAround(position.x, position.y, position.z, 64.0, pokemonEntity.level().dimension())
        MegaEvolutionEventHandler.transformAt[pokemonEntity] = Instant.now().plusMillis(3300)
    }
}