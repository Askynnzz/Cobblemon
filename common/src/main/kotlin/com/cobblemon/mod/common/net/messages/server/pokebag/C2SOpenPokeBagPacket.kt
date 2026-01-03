/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.pokebag

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class C2SOpenPokeBagPacket: NetworkPacket<C2SOpenPokeBagPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
    }

    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf) = C2SOpenPokeBagPacket()
        val ID = cobblemonResource("c2s_open_poke_bag")
    }
}