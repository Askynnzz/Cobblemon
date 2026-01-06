package com.cobblemon.mod.common.client.render.bedrock

import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState

open class SimplePosableState : PosableState() {

    override val schedulingTracker = SchedulingTracker()

    override fun getEntity() = null

    override fun updatePartialTicks(partialTicks: Float) {
        this.currentPartialTicks = partialTicks
        schedulingTracker.update(0F)
    }

}
