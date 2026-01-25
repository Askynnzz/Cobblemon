/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.tower

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battlefactory.BattleFactoryTowerManager
import com.cobblemon.mod.common.api.battlefactory.TowerDifficulty
import com.cobblemon.mod.common.api.battlefactory.TowerPokemonCache
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.tower.TowerPokemonSelectionPacket
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

/**
 * Server-side handler for processing the Tower Pokemon selection from the client.
 * 
 * Validates the selection and starts the Tower session with the chosen Pokemon.
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
object TowerPokemonSelectionHandler : ServerNetworkPacketHandler<TowerPokemonSelectionPacket> {
    
    override fun handle(packet: TowerPokemonSelectionPacket, server: MinecraftServer, player: ServerPlayer) {
        // Validate selection count
        if (packet.selectedIndices.size != 3) {
            player.sendSystemMessage(Component.literal("§cYou must select exactly 3 Pokémon!"))
            return
        }
        
        // Validate indices are in range [0, 5]
        if (packet.selectedIndices.any { it < 0 || it > 5 }) {
            player.sendSystemMessage(Component.literal("§cInvalid Pokémon selection!"))
            Cobblemon.LOGGER.warn("Player ${player.name.string} sent invalid indices: ${packet.selectedIndices}")
            return
        }
        
        // Validate indices are unique
        if (packet.selectedIndices.toSet().size != 3) {
            player.sendSystemMessage(Component.literal("§cYou cannot select the same Pokémon multiple times!"))
            return
        }
        
        // Get cached Pokemon
        val offeredPokemon = TowerPokemonCache.get(player.uuid)
        if (offeredPokemon == null) {
            player.sendSystemMessage(Component.literal("§cSession expired. Please start again."))
            Cobblemon.LOGGER.warn("No cached Pokemon for player ${player.name.string}")
            return
        }
        
        // Select the chosen Pokemon
        val selectedPokemon = packet.selectedIndices.map { offeredPokemon[it] }
        
        // Parse difficulty from config
        val config = BattleFactoryTowerManager.getConfig()
        val difficulty = config.getDifficulty(packet.difficulty) ?: run {
            player.sendSystemMessage(Component.literal("§cInvalid difficulty!"))
            Cobblemon.LOGGER.error("Invalid difficulty ID: ${packet.difficulty}")
            TowerPokemonCache.remove(player.uuid)
            return
        }
        
        // Clean up cache
        TowerPokemonCache.remove(player.uuid)
        
        // Start the tower session
        BattleFactoryTowerManager.onPokemonSelected(player, difficulty, selectedPokemon)
    }
}
