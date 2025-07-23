/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readItemStack
import com.cobblemon.mod.common.util.readText
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class DeltaBattleActorTeamPacket(val actor: UUID, val team: List<DeltaBattlePokemonDTO>): NetworkPacket<DeltaBattleActorTeamPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(actor)
        buffer.writeCollection(team) { pb, value ->
            pb.writeUUID(value.uuid)
            pb.writeBoolean(value.fainted)
        }
    }

    companion object {
        val ID = cobblemonResource("delta_battle_team")
        fun decode(buffer: RegistryFriendlyByteBuf) = DeltaBattleActorTeamPacket(
            actor = buffer.readUUID(),
            team = buffer.readList {
                DeltaBattlePokemonDTO(
                    uuid = buffer.readUUID(),
                    fainted = buffer.readBoolean(),
                    moves = buffer.readList { buffer.readNullable { DeltaMoveDTO(buffer.readText(), buffer.readInt()) } },
                    heldItem = buffer.readNullable { buffer.readItemStack() },
                    buffs = buffer.readMap({ buffer.readEnum(Stats::class.java) }, { buffer.readDouble() })
                )
            }
        )
    }
}