package com.cobblemon.mod.common.client.battle.preview

import com.cobblemon.mod.common.net.messages.client.battle.BattlePokemonDTO
import net.minecraft.network.chat.Component
import java.time.Instant

class ClientBattleTeamPreview(
    val selections: Int,
    val team: List<BattlePokemonDTO?>,
    val opponent: List<BattlePokemonDTO?>,
    val mustPickBy: Instant,
    val opponentName: Component
) {
    val selection = mutableListOf<BattlePokemonDTO>()

    fun confirm() {
        println("confirm")
    }

    fun cancel() {
        println("cancel")
    }
}