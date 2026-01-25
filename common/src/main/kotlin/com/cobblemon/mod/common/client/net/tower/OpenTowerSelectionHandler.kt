/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.tower

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.tower.TowerPokemonSelectionScreen
import com.cobblemon.mod.common.net.messages.client.tower.OpenTowerSelectionPacket
import net.minecraft.client.Minecraft

/**
 * Client-side handler for opening the Tower Pokemon selection GUI.
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
object OpenTowerSelectionHandler : ClientNetworkPacketHandler<OpenTowerSelectionPacket> {
    
    override fun handle(packet: OpenTowerSelectionPacket, client: Minecraft) {
        client.setScreen(TowerPokemonSelectionScreen(packet.offeredPokemon, packet.difficulty))
    }
}
