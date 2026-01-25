/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.tower

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent from client to server with the player's Pokemon selection.
 * 
 * Contains the indices of the 3 Pokemon selected from the offered 6.
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
class TowerPokemonSelectionPacket(
    val selectedIndices: List<Int>,
    val difficulty: String
) : NetworkPacket<TowerPokemonSelectionPacket> {
    
    companion object {
        val ID = cobblemonResource("tower_pokemon_selection")
        
        fun decode(buffer: RegistryFriendlyByteBuf): TowerPokemonSelectionPacket {
            return TowerPokemonSelectionPacket(
                selectedIndices = buffer.readList { it.readInt() },
                difficulty = buffer.readUtf()
            )
        }
    }
    
    override val id = ID
    
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(selectedIndices) { buf, index -> buf.writeInt(index) }
        buffer.writeUtf(difficulty)
    }
}
