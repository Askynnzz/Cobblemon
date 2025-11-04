package com.cobblemon.mod.common.client.battle.preview

import com.cobblemon.mod.common.net.messages.client.battle.BattlePokemonDTO

class ClientBattleTeamPreview(val selections: Int, val team: List<BattlePokemonDTO?>, val opponent: List<BattlePokemonDTO?>) {
    val selection = mutableListOf<BattlePokemonDTO>()

    fun confirm() {
        TODO()
    }

    fun cancel() {
        TODO()
    }
}