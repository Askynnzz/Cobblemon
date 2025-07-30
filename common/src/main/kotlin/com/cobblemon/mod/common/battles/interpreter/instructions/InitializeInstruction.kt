/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.dispatch.DispatchResult
import com.cobblemon.mod.common.battles.dispatch.InstructionSet
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.net.messages.client.battle.BattleInitializePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleMusicPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleSetTeamPokemonPacket
import com.cobblemon.mod.common.net.messages.client.battle.DeltaBattlePokemonDTO
import com.cobblemon.mod.common.net.messages.client.battle.DeltaBattleActorTeamPacket
import com.cobblemon.mod.common.net.messages.client.battle.DeltaMoveDTO

/**
 * Format: |start
 *
 * Indicates that the battle has started.
 * @author Segfault Guy
 * @since July 24th, 2024
 */
class InitializeInstruction(val instructionSet: InstructionSet, val message: BattleMessage): InterpreterInstruction {
    override fun invoke(battle: PokemonBattle) {
        val incoming = instructionSet.getSubsequentInstructions(this).filterIsInstance<SwitchInstruction>()
        incoming.forEach {
            // TODO redundant, make these SwitchInstruction properties
            val (_, activePokemon) = it.publicMessage.actorAndActivePokemon(0, battle) ?: return@forEach
            val illusion = it.publicMessage.battlePokemonFromOptional(battle, "is")
            val pokemon = it.publicMessage.battlePokemon(0, battle) ?: return@forEach
            pokemon.entity?.let {
                // If a Pokémon entity is being recalled with an animation,
                // wrap up the animation and recall the Pokémon immediately.
                if (it.beamMode == 3 && battle.battlePartyStores.isEmpty()) {
                    pokemon.effectedPokemon.recall()
                }
                // If already on the field, initialize for the BattleInitializePacket
                else {
                    activePokemon.battlePokemon = pokemon
                    activePokemon.illusion = illusion
                }
            }
        }

        battle.actors.filterIsInstance<PlayerBattleActor>().forEach { actor ->
            val initializePacket = BattleInitializePacket(battle, actor.getSide())
            actor.sendUpdate(initializePacket)
            actor.sendUpdate(BattleMusicPacket(actor.battleTheme))
        }

        battle.actors.forEach { actor ->
            actor.sendUpdate(BattleSetTeamPokemonPacket(actor.pokemonList.map { it.effectedPokemon }))

            actor.sendUpdate(DeltaBattleActorTeamPacket(actor.uuid, actor.pokemonList.map { pokemon ->
                val boostMultipliers = pokemon.boosts.mapValues { pokemon.getBoostMultiplier(it.key) }.filter { it.value != 1.0 }
                val moves = pokemon.moveSet.getMovesWithNulls().map { move ->
                    if (move == null) return@map null
                    return@map DeltaMoveDTO(move.displayName, 0)
                }
                DeltaBattlePokemonDTO(
                    pokemon.uuid,
                    pokemon.effectedPokemon.isFainted(),
                    pokemon.effectedPokemon.ability.name,
                    moves,
                    pokemon.effectedPokemon.heldItem,
                    boostMultipliers,
                    speed = pokemon.effectedPokemon.speed,
                    BattleInitializePacket.ActiveBattlePokemonDTO.fromPokemon(pokemon, true, pokemon.getIllusion())
                )
            }))

            val otherActors = battle.actors.filter { it != actor }

            otherActors.forEach { other ->
                val packet = DeltaBattleActorTeamPacket(actor.uuid, actor.pokemonList.map { pokemon ->
                    val boostMultipliers = pokemon.boosts.mapValues { pokemon.getBoostMultiplier(it.key) }.filter { it.value != 1.0 }

                    val activeBattlePokemon = battle.activePokemon.find { it.battlePokemon?.uuid == pokemon.uuid }?.battlePokemon
                    val activeBattlePokemonDTO = activeBattlePokemon?.let {
                        BattleInitializePacket.ActiveBattlePokemonDTO.fromPokemon(it, false, it.getIllusion())
                    }

                    DeltaBattlePokemonDTO(
                        pokemon.uuid,
                        pokemon.effectedPokemon.isFainted(),
                        pokemon.revealedAbility,
                        pokemon.revealedMoves,
                        pokemon.revealedHeldItem,
                        boostMultipliers,
                        speed = null,
                        activeBattlePokemonDTO
                    )
                })
                other.sendUpdate(packet)
            }
            val req = actor.request ?: return@forEach
            actor.sendUpdate(BattleQueueRequestPacket(req))
        }

        battle.dispatch {
            DispatchResult { !battle.side1.stillSendingOut() && !battle.side2.stillSendingOut() }
        }

        battle.dispatchGo {
            battle.started = true
            battle.side1.playCries()
            afterOnServer(seconds = 1.0F) { battle.side2.playCries() }
        }
    }
}