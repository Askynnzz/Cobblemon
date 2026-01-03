/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric

import com.cobblemon.mod.common.CobblemonItems
import com.misterpemodder.shulkerboxtooltip.api.ShulkerBoxTooltipApi
import com.misterpemodder.shulkerboxtooltip.api.provider.BlockEntityPreviewProvider
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProviderRegistry
import net.minecraft.resources.ResourceLocation

object CobblemonTooltips : ShulkerBoxTooltipApi {
    override fun registerProviders(registry: PreviewProviderRegistry) {
        registry.register(ResourceLocation.parse("cobblemon:small_poke_bag"), BlockEntityPreviewProvider(CobblemonItems.SMALL_POKE_BAG.size * 9, false), CobblemonItems.SMALL_POKE_BAG)
        registry.register(ResourceLocation.parse("cobblemon:medium_poke_bag"), BlockEntityPreviewProvider(CobblemonItems.MEDIUM_POKE_BAG.size * 9, false), CobblemonItems.MEDIUM_POKE_BAG)
        registry.register(ResourceLocation.parse("cobblemon:large_poke_bag"), BlockEntityPreviewProvider(CobblemonItems.LARGE_POKE_BAG.size * 9, false), CobblemonItems.LARGE_POKE_BAG)
        registry.register(ResourceLocation.parse("cobblemon:huge_poke_bag"), BlockEntityPreviewProvider(CobblemonItems.HUGE_POKE_BAG.size * 9, false), CobblemonItems.HUGE_POKE_BAG)
    }
}