/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeNullable
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class DeltaBattleInformationPacket(val battle: UUID, val informationDTO: DeltaBattleInformationDTO): NetworkPacket<DeltaBattleInformationPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(battle)
        buffer.writeInt(informationDTO.turn)
        buffer.writeNullable(informationDTO.weather) { _, value -> buffer.writeString(value) }
        buffer.writeNullable(informationDTO.terrain) { _, value -> buffer.writeString(value) }
        buffer.writeNullable(informationDTO.room) { _, value -> buffer.writeString(value) }
    }

    companion object {
        val ID = cobblemonResource("delta_battle_information")
        fun decode(buffer: RegistryFriendlyByteBuf) = DeltaBattleInformationPacket(
            battle = buffer.readUUID(),
            informationDTO = DeltaBattleInformationDTO(
                turn = buffer.readInt(),
                weather = buffer.readNullable { buffer.readString() },
                terrain = buffer.readNullable { buffer.readString() },
                room = buffer.readNullable { buffer.readString() }
            )
        )
    }
}

data class DeltaBattleInformationDTO(
    val turn: Int,
    val weather: String?,
    val terrain: String?,
    val room: String?
)