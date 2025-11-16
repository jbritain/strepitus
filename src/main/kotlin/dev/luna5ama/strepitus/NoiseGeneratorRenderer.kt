package dev.luna5ama.strepitus

import dev.luna5ama.glwrapper.ShaderBindingSpecs
import dev.luna5ama.glwrapper.ShaderProgram
import dev.luna5ama.glwrapper.ShaderSource
import dev.luna5ama.glwrapper.base.*
import dev.luna5ama.glwrapper.enums.BufferTarget
import dev.luna5ama.glwrapper.enums.FilterMode
import dev.luna5ama.glwrapper.enums.ImageFormat
import dev.luna5ama.glwrapper.enums.WrapMode
import dev.luna5ama.glwrapper.objects.BufferObject
import dev.luna5ama.glwrapper.objects.TextureObject
import dev.luna5ama.strepitus.gl.register
import kotlin.math.pow

class NoiseGeneratorRenderer(
    private val widthProvider: () -> Int,
    private val heightProvider: () -> Int,
) : AbstractRenderer() {
    var mainParameters = MainParameters()
    var outputProcessingParameters = OutputProcessingParameters()
    var viewerParameters = ViewerParameters()
    var noiseLayers = emptyList<NoiseLayerParameters<*>>()

    var frameWidth = 0
    var frameHeight = 0

    val windowWidth: Int
        get() = widthProvider()

    val windowHeight: Int
        get() = heightProvider()

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
        sampler("usam_outputImageTiling", outputImage, samplerManager.get {
            filter(FilterMode.Linear, FilterMode.Linear)
            wrap(WrapMode.Repeat, WrapMode.Repeat, WrapMode.Repeat)
        })
        sampler("usam_outputImage", outputImage, samplerManager.get {
            filter(FilterMode.Linear, FilterMode.Linear)
            wrap(WrapMode.ClampToBorder, WrapMode.ClampToBorder, WrapMode.ClampToBorder)
            borderColor(0.0f, 0.0f, 0.0f, 0.0f)
        })
        buffer("DataBuffer", dataBuffer, BufferTarget.ShaderStorage)
    }

    private val generateNoiseShader = register(ShaderProgram(ShaderSource.Comp("GenerateNoise.comp.glsl")))
    private val resetCounterShader = register(ShaderProgram(ShaderSource.Comp("ResetCounter.comp.glsl")))
    private val countRangeShader = register(ShaderProgram(ShaderSource.Comp("CountRange.comp.glsl")))
    private val normalizeShaders = object : ShaderProgram.Variants<GPUFormat>() {
        override fun create(key: GPUFormat): ShaderProgram {
            return register(ShaderProgram(ShaderSource.Comp("Normalize.comp.glsl") {
                define("OUTPUT_IMAGE_FORMAT", key.glslFormat)
            }))
        }
    }

    private val finalBlitShader = register(
        ShaderProgram(
            ShaderSource.Vert("Blit.vert.glsl"),
            ShaderSource.Frag("Blit.frag.glsl")
        )
    )

    override fun draw() {
        noiseImage.destroy()
        outputImage.destroy()

        noiseImage.allocate(
            1,
            ImageFormat.R32G32B32A32_F,
            mainParameters.width,
            mainParameters.height,
            mainParameters.slices
        )
        outputImage.allocate(
            1,
            outputProcessingParameters.format.gpuFormat.value,
            mainParameters.width,
            mainParameters.height,
            mainParameters.slices
        )

        resetCounterShader.bind()
        resetCounterShader.applyBinding(bindings)
        glDispatchCompute(1, 1, 1)

        generateNoiseShader.bind()
        generateNoiseShader.applyBinding(bindings)
        generateNoiseShader.uniform3f(
            "uval_noiseTexSizeF",
            mainParameters.width.toFloat(),
            mainParameters.height.toFloat(),
            mainParameters.slices.toFloat()
        )
        glDispatchCompute(mainParameters.width / 16, mainParameters.height / 16, mainParameters.slices)

        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT)

        countRangeShader.bind()
        countRangeShader.applyBinding(bindings)
        glDispatchCompute(mainParameters.width / 32, mainParameters.height / 32, mainParameters.slices)
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT)

        val normalizeShader = normalizeShaders[outputProcessingParameters.format.gpuFormat]
        normalizeShader.bind()
        normalizeShader.applyBinding(bindings)
        normalizeShader.uniform1i("uval_normalize", if (outputProcessingParameters.normalize) 1 else 0)
        normalizeShader.uniform1f("uval_minVal", outputProcessingParameters.minVal.toFloat())
        normalizeShader.uniform1f("uval_maxVal", outputProcessingParameters.maxVal.toFloat())
        normalizeShader.uniform1i("uval_flip", if (outputProcessingParameters.flip) 1 else 0)
        normalizeShader.uniform1i("uval_dither", if (outputProcessingParameters.dither) 1 else 0)
        glDispatchCompute(mainParameters.width / 16, mainParameters.height / 16, mainParameters.slices)
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glViewport(windowWidth - frameWidth, 0, frameWidth, frameHeight)
        finalBlitShader.bind()
        finalBlitShader.applyBinding(bindings)
        finalBlitShader.uniform3f(
            "uval_noiseTexSizeF",
            mainParameters.width.toFloat(),
            mainParameters.height.toFloat(),
            mainParameters.slices.toFloat()
        )
        finalBlitShader.uniform1f("uval_slice", viewerParameters.slice.toFloat())
        finalBlitShader.uniform1f("uval_zoom", 2.0.pow(-viewerParameters.zoom.toDouble()).toFloat())
        finalBlitShader.uniform1i("uval_colorMode", viewerParameters.colorMode.ordinal)
        finalBlitShader.uniform1i("uval_tilling", if (viewerParameters.tilling) 1 else 0)
        var offsetX = frameWidth / 2.0 - mainParameters.width / 2.0
        offsetX += windowWidth - frameWidth
        offsetX -= viewerParameters.centerX.toDouble()
        var offsetY = frameHeight / 2.0 - mainParameters.height / 2.0
        offsetY += viewerParameters.centerY.toDouble()
        finalBlitShader.uniform2f("uval_offset",offsetX.toFloat(), offsetY.toFloat())
        basic.drawQuad()
    }
}