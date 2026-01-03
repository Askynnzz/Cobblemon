/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokebag

import net.minecraft.server.level.ServerPlayer

/**
 * Trinkets is a Fabric only mod. I still want the pokebag items in common with the rest of the items.
 * This is a bit weird, but since we only care about this fork being functional on Fabric,
 * we add a layer of indirection here to keep the dependency within the fabric folder.
 *
 * We could improve this in a couple ways but this is the easiest solution on a pretty tight time constraint.
 *
 * See CobblemonFabric#registerTrinketEvents for how this is handled.
 *
 * @author landonjw
 */
data class PokeBagOpenRequestEvent(val player: ServerPlayer)