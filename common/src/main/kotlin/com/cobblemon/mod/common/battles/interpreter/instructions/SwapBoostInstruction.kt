/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-swapboost|SOURCE|TARGET|STATS
 *
 * Swaps the boosts from STATS between the SOURCE Pokémon and TARGET Pokémon.
 * @author Renaissance
 * @since March 24th, 2023
 */
class SwapBoostInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting(2F) {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatchWaiting
            val pokemonName = pokemon.getName()
            val targetPokemon = message.battlePokemon(1, battle) ?: return@dispatchWaiting
            val targetPokemonName = targetPokemon.getName()
            val effectID = message.effect()?.id ?: return@dispatchWaiting
            val lang = when (effectID) {
                "guardswap", "powerswap", "heartswap" -> battleLang("swapboost.$effectID", pokemonName)
                else -> battleLang("swapboost.generic", pokemonName, targetPokemonName)
            }
            applyBoostInformation(effectID, pokemon, targetPokemon)
            pokemon.sendUpdate()
            targetPokemon.sendUpdate()
            battle.broadcastChatMessage(lang)
            pokemon.contextManager.swap(targetPokemon.contextManager, BattleContext.Type.BOOST)
            pokemon.contextManager.swap(targetPokemon.contextManager, BattleContext.Type.UNBOOST)
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }

    fun applyBoostInformation(effect: String, source: BattlePokemon, target: BattlePokemon) {
        when (effect) {
            "guardswap" -> {
                switchBoostInformation(source, target, Stats.DEFENCE)
                switchBoostInformation(source, target, Stats.SPECIAL_DEFENCE)
            }
            "powerswap" -> {
                switchBoostInformation(source, target, Stats.ATTACK)
                switchBoostInformation(source, target, Stats.SPECIAL_ATTACK)
            }
            "heartswap" -> {
                Stats.entries.forEach { stat ->
                    switchBoostInformation(source, target, stat)
                }
            }
            else -> return
        }
    }

    fun switchBoostInformation(source: BattlePokemon, target: BattlePokemon, stat: Stats) {
        val sourceBoost = source.boosts[stat]
        val targetBoost = target.boosts[stat]

        if (sourceBoost == null) {
            target.boosts.remove(stat)
        }
        else {
            target.boosts[stat] = sourceBoost
        }

        if (targetBoost == null) {
            source.boosts.remove(stat)
        }
        else {
            source.boosts[stat] = targetBoost
        }
    }

}