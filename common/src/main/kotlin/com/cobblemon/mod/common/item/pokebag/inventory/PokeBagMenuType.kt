/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.pokebag.inventory

import net.minecraft.world.inventory.MenuType

object PokeBagMenuType {

    val SMALL_POKE_BAG = MenuType.register("small_poke_bag", PokeBagMenu::small)
    val MEDIUM_POKE_BAG = MenuType.register("medium_poke_bag", PokeBagMenu::medium)
    val LARGE_POKE_BAG = MenuType.register("large_poke_bag", PokeBagMenu::large)
    val HUGE_POKE_BAG = MenuType.register("huge_poke_bag", PokeBagMenu::huge)

    fun initialise() {}
}