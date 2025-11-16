package dev.luna5ama.strepitus

import dev.luna5ama.strepitus.gl.IGLObjContainer
import dev.luna5ama.strepitus.gl.SamplerManager
import dev.luna5ama.strepitus.gl.register

abstract class AbstractRenderer : IGLObjContainer by IGLObjContainer.Impl() {
    val samplerManager = register(SamplerManager())

    abstract fun draw()
}