package com.cobblemon.mod.common.client.battle

import com.cobblemon.mod.common.net.messages.client.battle.BattleInformationDTO
import com.cobblemon.mod.common.net.messages.client.battle.BattlePokemonDTO
import java.time.Instant
import java.util.UUID

object ClientBattleInformationRepository {
    val actors = mutableMapOf<UUID, MutableList<BattlePokemonDTO>>()
    val battles = mutableMapOf<UUID, BattleInformationDTO>()
    val actorActualSides = mutableMapOf<UUID, Int>()
    var mustChooseBy: Instant? = null
}