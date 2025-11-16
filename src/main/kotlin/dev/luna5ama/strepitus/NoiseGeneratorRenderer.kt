package dev.luna5ama.strepitus

import dev.luna5ama.glwrapper.ShaderBindingSpecs
import dev.luna5ama.glwrapper.ShaderProgram
import dev.luna5ama.glwrapper.ShaderSource
import dev.luna5ama.glwrapper.base.glDispatchCompute
import dev.luna5ama.glwrapper.enums.BufferTarget
import dev.luna5ama.glwrapper.enums.FilterMode
import dev.luna5ama.glwrapper.enums.ImageFormat
import dev.luna5ama.glwrapper.enums.WrapMode
import dev.luna5ama.glwrapper.objects.BufferObject
import dev.luna5ama.glwrapper.objects.TextureObject
import dev.luna5ama.strepitus.gl.register

class NoiseGeneratorRenderer(
    var mainParameters: MainParameters,
    var outputProcessingParameters: OutputProcessingParameters,
    var viewerParameters: ViewerParameters,
) : AbstractRenderer() {
    private val noiseImage = register(TextureObject.Texture3D()).apply {
        label = "noiseImage"
    }

    private val outputImage = register(TextureObject.Texture3D()).apply {
        label = "outputImage"
    }

    private val dataBuffer = register(BufferObject.Immutable()).apply {
        label = "dataBuffer"
        allocate(16L, 0)
    }

    private val bindings = ShaderBindingSpecs.of {
        image("uimg_noiseImage", noiseImage)
        image("uimg_outputImage", outputImage)
        sampler("usam_outputImage", outputImage, samplerManager.get {
            filter(FilterMode.Linear, FilterMode.Linear)
            wrap(WrapMode.Repeat, WrapMode.Repeat)
        })
        buffer("DataBuffer", dataBuffer, BufferTarget.ShaderStorage)
    }

    private val resetCounterShader = register(ShaderProgram(ShaderSource.Comp("ResetCounter.comp.glsl")))
    private val countRangeShader = register(ShaderProgram(ShaderSource.Comp("CountRange.comp.glsl")))

    override fun draw() {
        noiseImage.allocate(
            1,
            ImageFormat.R32G32B32A32_F,
            mainParameters.height,
            mainParameters.width,
            mainParameters.slices
        )
        outputImage.allocate(
            1,
            outputProcessingParameters.format.value,
            mainParameters.height,
            mainParameters.width,
            mainParameters.slices
        )

        resetCounterShader.bind()
        resetCounterShader.applyBinding(bindings)
        glDispatchCompute(1, 1, 1)
    }
}