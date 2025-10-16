package com.cobblemon.mod.common.battles.timers

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.ForfeitActionResponse
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.util.getPlayer
import java.time.Duration
import java.time.Instant

class ShowdownTimer(val battle: PokemonBattle, val actor: PlayerBattleActor) {
    private lateinit var turnStart: Instant
    private var secondsRemaining = 210
    private var hasSelected = false

    init {
        checkForStreamerIncrease()
    }

    /**
     * For streamers, we introduce a permission that doubles this timer as a deterrence of stream sniping.
     * We also have a similar system for notifying streamers when the opponent has used a move (so they can choose last.)
     */
    fun checkForStreamerIncrease() {
        val player = actor.uuid.getPlayer() ?: return
        if (!Cobblemon.permissionValidator.hasPermission(player, "battle.timer.streamer", 4)) return
        secondsRemaining *= 2
    }

    fun tick() {
        if (hasSelected) return
        val secondsSinceStart = Duration.between(turnStart, Instant.now()).seconds
        if (secondsRemaining - secondsSinceStart <= 0) timeout()
    }

    fun selection() {
        val secondsSinceStart = Duration.between(turnStart, Instant.now()).seconds.toInt()
        secondsRemaining = secondsRemaining - secondsSinceStart + 10
        hasSelected = true
    }

    fun startTurn() {
        turnStart = Instant.now()
        hasSelected = false
    }

    fun timeout() {
        actor.setActionResponses(listOf(ForfeitActionResponse()))
    }

    fun mustChooseBy(): Instant {
        return Instant.now().plusSeconds(secondsRemaining.toLong())
    }
}