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