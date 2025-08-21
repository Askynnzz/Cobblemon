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
import com.cobblemon.mod.common.api.battles.interpreter.InvalidInstructionException
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-setboost|POKEMON|STAT|AMOUNT
 *
 * Same as -boost and -unboost, but STAT is set to AMOUNT instead of boosted by AMOUNT.\
 * @author Renaissance
 * @since February 16th, 2023
 */
class SetBoostInstruction(val message: BattleMessage): InterpreterInstruction {

    val statKey = message.argumentAt(1) ?: throw InvalidInstructionException(message)
    val stages = message.argumentAt(2)?.toInt() ?: throw InvalidInstructionException(message)
    val stat = Stats.getStat(statKey)

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting(1.5F) {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatchWaiting
            val pokemonName = pokemon.getName()
            val effectID = message.effect()?.id ?: return@dispatchWaiting
            val lang = battleLang("setboost.$effectID", pokemonName)
            pokemon.boosts[stat] = stages
            pokemon.sendUpdate()
            battle.broadcastChatMessage(lang)
            pokemon.contextManager.add(ShowdownInterpreter.getContextFromAction(message, BattleContext.Type.BOOST, battle))
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}