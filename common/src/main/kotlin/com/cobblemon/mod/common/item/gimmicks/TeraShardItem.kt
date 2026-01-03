/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.gimmicks

import com.cobblemon.mod.common.api.types.tera.TeraType
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties

class TeraShardItem(val type: TeraType) : Item(Properties().stacksTo(64))