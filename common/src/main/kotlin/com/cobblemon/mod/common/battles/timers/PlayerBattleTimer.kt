package com.cobblemon.mod.common.battles.timers

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.ForfeitActionResponse
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import java.time.Instant

interface PlayerBattleTimer {
    val actor: PlayerBattleActor
    val battle: PokemonBattle

    fun startTurn()
    fun selection()
    fun tick()
    fun mustChooseBy(): Instant

    fun timeout() {
        actor.setActionResponses(listOf(ForfeitActionResponse()))
    }
}