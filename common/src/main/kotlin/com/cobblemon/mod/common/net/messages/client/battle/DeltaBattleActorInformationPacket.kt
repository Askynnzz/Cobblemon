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
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.readText
import com.cobblemon.mod.common.util.writeItemStack
import com.cobblemon.mod.common.util.writeString
import com.cobblemon.mod.common.util.writeText
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import java.util.*

class DeltaBattleActorInformationPacket(val actor: UUID, val update: DeltaBattlePokemonDTO): NetworkPacket<DeltaBattleActorInformationPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(actor)
        buffer.writeUUID(update.uuid)
        buffer.writeBoolean(update.fainted)
        buffer.writeNullable (update.ability) { _, v -> buffer.writeString(v) }
        buffer.writeCollection(update.moves) { _, v ->
            buffer.writeNullable(v) { _, dto ->
                buffer.writeText(dto.move)
                buffer.writeInt(dto.timesUsed)
            }
        }
        buffer.writeNullable(update.heldItem) { _, item -> buffer.writeItemStack(item) }
        buffer.writeMap(update.buffs, { _, v -> buffer.writeEnum(v) }, { _, v -> buffer.writeDouble(v) })
        buffer.writeNullable(update.speed) { _, v -> buffer.writeInt(v) }
        buffer.writeNullable(update.activeBattlePokemonDTO) { _, v -> v.saveToBuffer(buffer) }
    }

    companion object {
        val ID = cobblemonResource("delta_battle_actor_information")
        fun decode(buffer: RegistryFriendlyByteBuf) = DeltaBattleActorInformationPacket(
            actor = buffer.readUUID(),
            update = DeltaBattlePokemonDTO(
                uuid = buffer.readUUID(),
                fainted = buffer.readBoolean(),
                ability = buffer.readNullable { buffer.readString() },
                moves = buffer.readList { buffer.readNullable { DeltaMoveDTO(buffer.readText(), buffer.readInt()) } },
                heldItem = buffer.readNullable { buffer.readItemStack() },
                buffs = buffer.readMap({ buffer.readEnum(Stats::class.java) }, { buffer.readDouble() }),
                speed = buffer.readNullable { buffer.readInt() },
                activeBattlePokemonDTO = buffer.readNullable { BattleInitializePacket.ActiveBattlePokemonDTO.loadFromBuffer(buffer) }
            )
        )
    }
}

data class DeltaBattlePokemonDTO(
    val uuid: UUID,
    val fainted: Boolean,
    val ability: String?,
    val moves: List<DeltaMoveDTO?>,
    val heldItem: ItemStack?,
    val buffs: Map<Stats, Double>,
    val speed: Int?,
    val activeBattlePokemonDTO: BattleInitializePacket.ActiveBattlePokemonDTO?
)

data class DeltaMoveDTO(
    val move: Component,
    var timesUsed: Int
)