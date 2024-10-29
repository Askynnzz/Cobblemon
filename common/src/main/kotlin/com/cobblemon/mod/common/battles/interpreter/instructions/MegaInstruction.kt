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
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.instruction.MegaEvolutionEvent
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.sendParticlesServer
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.phys.Vec3

/**
 * Format: |-mega|POKEMON|MEGASTONE
 *
 * POKEMON used item MEGASTONE to Mega Evolve.
 * @author Segfault Guy
 * @since September 10th, 2023
 */
class MegaInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val battlePokemon = message.battlePokemon(0, battle) ?: return
        val speciesName = battlePokemon.effectedPokemon.species.translatedName
        battlePokemon.entity?.let { entity ->
            entity.level().sendParticlesServer(ParticleTypes.EXPLOSION, entity.position().add(0.0, 1.0, 0.0), 10, Vec3(0.5, 0.5, 0.5), 0.4)
            entity.level().sendParticlesServer(ParticleTypes.LARGE_SMOKE, entity.position().add(0.0, 1.0, 0.0), 10, Vec3(0.5, 0.5, 0.5), 0.4)
            entity.level().playSoundServer(entity.position(), SoundEvents.GENERIC_EXPLODE.value())
        }
        battle.dispatchWaiting {
            val pokemonName = battlePokemon.getName()
            battle.broadcastChatMessage(battleLang("mega", pokemonName, speciesName).yellow())
            CobblemonEvents.MEGA_EVOLUTION.post(MegaEvolutionEvent(battle, battlePokemon))
            battle.minorBattleActions[battlePokemon.uuid] = message
            battlePokemon.entity?.cry()
        }
    }
}