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

object FriendshipCommand {

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal("friendship")
            .permission(CobblemonPermissions.FRIENDSHIP)
            .executes { execute(it.source) })
    }

    private fun execute(source: CommandSourceStack) : Int {
        try {
            val player = source.playerOrException
            player.sendPacket(TeamPreviewPacket(
                6,
                player.party().toGappyList(),
                listOf(
                    PokemonProperties.parse("ninetales").create(),
                    PokemonProperties.parse("slakoth").create(),
                    PokemonProperties.parse("bulbasaur").create(),
                    PokemonProperties.parse("togetic").create(),
                    PokemonProperties.parse("snivy").create(),
                    PokemonProperties.parse("wailord").create(),
                ),
                true
            ))
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return Command.SINGLE_SUCCESS
    }

}