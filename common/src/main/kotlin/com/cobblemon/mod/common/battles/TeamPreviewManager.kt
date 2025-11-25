/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.net.messages.client.battle.CloseTeamPreviewPacket
import com.cobblemon.mod.common.net.messages.client.battle.TeamPreviewPacket
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.safeParty
import net.minecraft.server.level.ServerPlayer
import java.time.Duration
import java.time.Instant

object TeamPreviewManager {
    private val previews = mutableListOf<TeamPreview>()
    val TEAM_PREVIEW_TIME_SECONDS = 60

    fun tick() {
        val expired = previews.filter { Duration.between(it.started, Instant.now()).seconds >= TEAM_PREVIEW_TIME_SECONDS }
        expired.forEach { cancel(it) }

        val ready = previews.filter {
            hasEligibleSelections(it.playerA, it.playerASelection, it.selections)
                    && hasEligibleSelections(it.playerB, it.playerBSelection, it.selections)
        }
        ready.forEach { start(it) }
    }

    fun add(playerA: ServerPlayer, playerB: ServerPlayer, selections: Int, openTeamSheet: Boolean, onStart: (TeamPreview) -> Unit, onCancel: (TeamPreview) -> Unit) {
        if (previews.any { it.playerA == playerA || it.playerB == playerB }) return
        val now = Instant.now()
        previews.add(TeamPreview(selections, playerA, playerB, setOf(), setOf(), now, onStart, onCancel))
        playerA.sendPacket(TeamPreviewPacket(
            selections,
            playerA.party().toGappyList(),
            playerB.party().toGappyList(),
            openTeamSheet,
            now.plusSeconds(TEAM_PREVIEW_TIME_SECONDS.toLong()),
            playerB.name
        ))
        playerB.sendPacket(TeamPreviewPacket(
            selections,
            playerB.party().toGappyList(),
            playerA.party().toGappyList(),
            openTeamSheet,
            now.plusSeconds(TEAM_PREVIEW_TIME_SECONDS.toLong()),
            playerA.name
        ))
    }

    fun remove(player: ServerPlayer) {
        val preview = previews.firstOrNull { it.playerA == player || it.playerB == player } ?: return
        cancel(preview)
    }

    fun cancel(preview: TeamPreview) {
        preview.onCancel(preview)
        preview.playerA.sendPacket(CloseTeamPreviewPacket())
        preview.playerB.sendPacket(CloseTeamPreviewPacket())
        previews.remove(preview)
    }

    fun start(preview: TeamPreview) {
        preview.onStart(preview)
        preview.playerA.sendPacket(CloseTeamPreviewPacket())
        preview.playerB.sendPacket(CloseTeamPreviewPacket())
        previews.remove(preview)
    }

    fun changeSelections(player: ServerPlayer, selections: Set<Int>) {
        val preview = previews.firstOrNull { it.playerA == player || it.playerB == player } ?: return
        val isPlayerA = preview.playerA == player
        previews.remove(preview)
        if (isPlayerA) {
            previews.add(preview.copy(playerASelection = selections))
        }
        else {
            previews.add(preview.copy(playerBSelection = selections))
        }
    }

    fun hasEligibleSelections(player: ServerPlayer, selections: Set<Int>, neededSelections: Int): Boolean {
        if (selections.size < neededSelections) return false
        val party = player.safeParty()?.toGappyList() ?: return false
        if (selections.any { party.size <= it || party[it] == null }) return false
        return true
    }
}

data class TeamPreview(
    val selections: Int,
    val playerA: ServerPlayer,
    val playerB: ServerPlayer,
    val playerASelection: Set<Int>,
    val playerBSelection: Set<Int>,
    val started: Instant,
    val onStart: (TeamPreview) -> Unit,
    val onCancel: (TeamPreview) -> Unit
)