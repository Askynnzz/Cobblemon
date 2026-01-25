/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import com.cobblemon.mod.common.api.dialogue.ActiveDialogue
import com.cobblemon.mod.common.api.dialogue.DialoguePredicate
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import net.minecraft.server.level.ServerPlayer

/**
 * MoLang predicate to check if player has active Battle Factory session.
 * 
 * Used in dialogue conditions: `q.has_battlefactory_session`
 * 
 * Returns true if the player has an active session, false otherwise.
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
class HasBattleFactorySessionPredicate : DialoguePredicate {
    
    override fun invoke(dialogue: ActiveDialogue): Boolean {
        return FacilitySessionManager.hasActiveSession(dialogue.playerEntity)
    }
    
    companion object {
        const val NAME = "has_battlefactory_session"
    }
}
