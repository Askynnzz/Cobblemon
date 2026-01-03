/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.interact.wheel.InteractWheelOption
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.gimmicks.MegaStoneItem
import com.cobblemon.mod.common.item.gimmicks.megaevolution.MegaStoneUtils
import com.cobblemon.mod.common.net.messages.server.megaevolution.C2SMegaEvolvePacket
import com.cobblemon.mod.common.net.messages.server.megaevolution.C2SRemoveMegaEvolutionPacket
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.safeParty
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import java.time.Instant

object MegaEvolutionEventHandler {

    val transformAt = mutableMapOf<PokemonEntity, Instant>()
    val megaEvolveInteractionWheel = cobblemonResource("textures/gui/interact/mega_evolve_interaction.png")
    val removeMegaEvolutionInteractionWheel = cobblemonResource("textures/gui/interact/un_mega_evolve_interaction.png")

    fun initialise() {
        CobblemonEvents.POKEMON_SENT_PRE.subscribe { event ->
            val pokemon = event.pokemon
            MegaStoneUtils.removeMegaEvolution(pokemon)
        }

        CobblemonEvents.POKEMON_RECALL_POST.subscribe { event ->
            val pokemon = event.pokemon
            MegaStoneUtils.removeMegaEvolution(pokemon)
        }

        CobblemonEvents.BATTLE_STARTED_PRE.subscribe { event ->
            event.battle.players.forEach { player ->
                player.safeParty()?.forEach { pokemon ->
                    MegaStoneUtils.removeMegaEvolution(pokemon)
                }
            }
        }

        CobblemonEvents.POKEMON_INTERACTION_GUI_CREATION.subscribe { event ->
            val pokemon = CobblemonClient.storage.party.firstOrNull { it?.entity?.uuid == event.pokemonID } ?: return@subscribe
            if (MegaStoneUtils.canApplyMegaEvolution(pokemon)) {
                event.addFillingOption(InteractWheelOption(
                    iconResource = megaEvolveInteractionWheel,
                    tooltipText = "interaction.megaevolution.mega_evolve",
                    onPress = {
                        CobblemonNetwork.sendToServer(C2SMegaEvolvePacket(event.pokemonID))
                        Minecraft.getInstance().setScreen(null)
                    }
                ))
            }
            else if (MegaStoneUtils.canRemoveMegaEvolution(pokemon)) {
                event.addFillingOption(InteractWheelOption(
                    iconResource = removeMegaEvolutionInteractionWheel,
                    tooltipText = "interaction.megaevolution.mega_evolve.remove",
                    onPress = {
                        CobblemonNetwork.sendToServer(C2SRemoveMegaEvolutionPacket(event.pokemonID))
                        Minecraft.getInstance().setScreen(null)
                    }
                ))
            }
        }

        PlatformEvents.SERVER_TICK_PRE.subscribe { server ->
            val toRemove = mutableListOf<PokemonEntity>()
            val now = Instant.now()
            transformAt.forEach { (entity, instant) ->
                if (!entity.isAlive) {
                    toRemove.add(entity)
                }
                else if (now.isAfter(instant)) {
                    val heldItem = entity.pokemon.heldItem().item
                    if (heldItem is MegaStoneItem && heldItem.canMegaEvolve(entity.pokemon)) {
                        heldItem.megaEvolve(entity)
                        entity.level().playSoundServer(entity.position(), SoundEvents.GENERIC_EXPLODE.value())
                        entity.cry()
                    }
                    toRemove.add(entity)
                }
            }
            toRemove.forEach { entity -> transformAt.remove(entity) }
        }
    }

}