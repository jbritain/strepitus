package dev.luna5ama.strepitus.gl

import dev.luna5ama.glwrapper.base.*
import dev.luna5ama.glwrapper.enums.CompareFunc
import dev.luna5ama.glwrapper.enums.CompareMode
import dev.luna5ama.glwrapper.enums.FilterMode
import dev.luna5ama.glwrapper.enums.WrapMode
import dev.luna5ama.glwrapper.objects.SamplerObject
import dev.luna5ama.kmogus.Ptr

class SamplerManager : IGLObjContainer by IGLObjContainer.Impl() {

    private val samplers = mutableMapOf<Key, SamplerObject>()

    fun get(block: Key.Builder.() -> Unit) = get(Key.Builder().apply(block).build())

    operator fun get(key: Key): SamplerObject {
        return samplers.getOrPut(key) {
            val sampler = SamplerObject()
            key.wrapS?.let { sampler.parameteri(GL_TEXTURE_WRAP_S, it.value) }
            key.wrapT?.let { sampler.parameteri(GL_TEXTURE_WRAP_T, it.value) }
            key.wrapR?.let { sampler.parameteri(GL_TEXTURE_WRAP_R, it.value) }
            key.minFilter?.let { sampler.parameteri(GL_TEXTURE_MIN_FILTER, it.value) }
            key.magFilter?.let { sampler.parameteri(GL_TEXTURE_MAG_FILTER, it.value) }
            key.borderColor?.let { (r, g, b, a) ->
                sampler.parameterfv(GL_TEXTURE_BORDER_COLOR, r, g, b, a)
            }
            key.minLod?.let { sampler.parameterf(GL_TEXTURE_MIN_LOD, it) }
            key.maxLod?.let { sampler.parameterf(GL_TEXTURE_MAX_LOD, it) }
            key.lodBias?.let { sampler.parameterf(GL_TEXTURE_LOD_BIAS, it) }
            key.compareMode?.let { sampler.parameteri(GL_TEXTURE_COMPARE_MODE, it.value) }
            key.compareFunc?.let { sampler.parameteri(GL_TEXTURE_COMPARE_FUNC, it.value) }
            key.anisotropy?.let { sampler.parameterf(GL_TEXTURE_MAX_ANISOTROPY, it) }
            sampler
        }
    }

    private val maxUnit = glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS)

    fun clearBinding() {
        glBindSamplers(0, maxUnit, Ptr.NULL)
    }

    override fun destroy() {
        samplers.values.forEach { it.destroy() }
    }

    data class Key private constructor(
        val wrapS: WrapMode?,
        val wrapT: WrapMode?,
        val wrapR: WrapMode?,
        val minFilter: FilterMode.Min?,
        val magFilter: FilterMode.Mag?,
        val borderColor: BorderColor?,
        val minLod: Float?,
        val maxLod: Float?,
        val lodBias: Float?,
        val compareMode: CompareMode?,
        val compareFunc: CompareFunc?,
        val anisotropy: Float?
    ) {
        data class BorderColor(
            val r: Float,
            val g: Float,
            val b: Float,
            val a: Float
        )

        class Builder {
            private var wrapS: WrapMode? = null
            private var wrapT: WrapMode? = null
            private var wrapR: WrapMode? = null
            private var minFilter: FilterMode.Min? = null
            private var magFilter: FilterMode.Mag? = null
            private var borderColor: BorderColor? = null
            private var minLod: Float? = null
            private var maxLod: Float? = null
            private var lodBias: Float? = null
            private var compareMode: CompareMode? = null
            private var compareFunc: CompareFunc? = null
            private var anisotropy: Float? = null

            fun filter(min: FilterMode.Min, mag: FilterMode.Mag) {
                minFilter = min
                magFilter = mag
            }

            fun mipLevels(base: Int, max: Int) {
                minLod = base.toFloat()
                maxLod = max.toFloat()
            }

            fun mipLod(min: Float, max: Float, bias: Float = 0.0f) {
                minLod = min
                maxLod = max
                lodBias = bias
            }

            fun anisotropy(anisotropy: Float) {
                this.anisotropy = anisotropy
            }

            fun wrap(s: WrapMode) {
                wrapS = s
            }

            fun wrap(s: WrapMode, t: WrapMode) {
                wrapS = s
                wrapT = t
            }

            fun wrap(s: WrapMode, t: WrapMode, r: WrapMode) {
                wrapS = s
                wrapT = t
                wrapR = r
            }

            fun borderColor(r: Float, g: Float, b: Float, a: Float) {
                borderColor = BorderColor(r, g, b, a)
            }

            fun compare(mode: CompareMode, func: CompareFunc) {
                compareMode = mode
                compareFunc = func
            }

            fun build() = Key(
                wrapS,
                wrapT,
                wrapR,
                minFilter,
                magFilter,
                borderColor,
                minLod,
                maxLod,
                lodBias,
                compareMode,
                compareFunc,
                anisotropy
            )
        }
    }
}