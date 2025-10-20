/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind.keybinds

import com.cobblemon.mod.common.CobblemonNetwork.sendToServer
import com.cobblemon.mod.common.client.keybind.CobblemonPartyLockedKeyBinding
import com.cobblemon.mod.common.client.keybind.KeybindCategories
import com.cobblemon.mod.common.net.messages.server.battle.SpectateNearestBattlePacket
import com.mojang.blaze3d.platform.InputConstants

object SpectateNearestBinding : CobblemonPartyLockedKeyBinding(
    "key.cobblemon.spectate_nearest",
    InputConstants.Type.KEYSYM,
    InputConstants.KEY_Y,
    KeybindCategories.COBBLEMON_CATEGORY
) {
    override fun onPress() {
        sendToServer(SpectateNearestBattlePacket())
    }
}