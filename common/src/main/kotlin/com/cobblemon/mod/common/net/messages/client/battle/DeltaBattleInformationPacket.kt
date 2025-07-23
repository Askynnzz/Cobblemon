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
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class DeltaBattleInformationPacket(val battle: UUID, val informationDTO: DeltaBattleInformationDTO): NetworkPacket<DeltaBattleInformationPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(battle)
        buffer.writeInt(informationDTO.turn)
    }

    companion object {
        val ID = cobblemonResource("delta_battle_information")
        fun decode(buffer: RegistryFriendlyByteBuf) = DeltaBattleInformationPacket(
            battle = buffer.readUUID(),
            informationDTO = DeltaBattleInformationDTO(
                turn = buffer.readInt()
            )
        )
    }
}

data class DeltaBattleInformationDTO(
    val turn: Int
)