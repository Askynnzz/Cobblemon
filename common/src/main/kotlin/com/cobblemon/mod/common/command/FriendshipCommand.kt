/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.battles.TeamPreviewManager
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType
import com.cobblemon.mod.common.net.messages.client.battle.TeamPreviewPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer
import java.time.Instant

object FriendshipCommand {

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal("friendship")
            .permission(CobblemonPermissions.FRIENDSHIP)
            .executes { execute(it.source) })
    }

    private fun execute(source: CommandSourceStack) : Int {
        try {
            val player = source.playerOrException
            val opponent = source.server.playerList.players.first { it != player }
            TeamPreviewManager.add(
                player,
                opponent,
                4,
                true,
                { println("STARTING BATTLE!") },
                {
                    println("CANCELLING BATTLE!")
                }
            )
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return Command.SINGLE_SUCCESS
    }

}