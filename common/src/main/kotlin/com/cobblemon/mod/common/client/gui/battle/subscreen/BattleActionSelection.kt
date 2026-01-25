/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.battle.subscreen

import com.cobblemon.mod.common.api.gui.ParentWidget
import com.cobblemon.mod.common.client.battle.SingleActionRequest
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.network.chat.MutableComponent

abstract class BattleActionSelection(
    val battleGUI: BattleGUI,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    name: MutableComponent
) : ParentWidget(x, y, width, height, name) {
    
    /**
     * Compatibility field for Mega Showdown mod.
     * Stores the SingleActionRequest if the object was created via the 7-parameter constructor.
     * This field must remain to maintain binary compatibility with Mega Showdown.
     */
    var request: SingleActionRequest? = null
    
    /**
     * Secondary constructor for compatibility with Mega Showdown mod.
     * Mega Showdown was built against vanilla Cobblemon 1.7.1 which had a different constructor signature.
     * This constructor accepts the old 7-parameter signature and delegates to the current 6-parameter constructor.
     * 
     * @param singleActionRequest The SingleActionRequest parameter (stored in request field for compatibility)
     */
    constructor(
        battleGUI: BattleGUI,
        singleActionRequest: SingleActionRequest,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        name: MutableComponent
    ) : this(battleGUI, x, y, width, height, name) {
        this.request = singleActionRequest
    }
    
    val opacity: Float
        get() = battleGUI.opacity

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if(pButton == InputConstants.MOUSE_BUTTON_LEFT){
            return mousePrimaryClicked(pMouseX, pMouseY)
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton)
    }

    abstract fun mousePrimaryClicked(pMouseX: Double, pMouseY: Double): Boolean
}