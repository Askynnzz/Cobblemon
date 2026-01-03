/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.megaevolution

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class C2SMegaEvolvePacket(val pokemon: UUID) : NetworkPacket<C2SMegaEvolvePacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pokemon)
    }
    companion object {
        val ID = cobblemonResource("c2s_mega_evolve")
        fun decode(buffer: RegistryFriendlyByteBuf) = C2SMegaEvolvePacket(buffer.readUUID())
    }
}