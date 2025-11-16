package dev.luna5ama.strepitus

import dev.luna5ama.strepitus.gl.IGLObjContainer
import dev.luna5ama.strepitus.gl.register
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
//import org.lwjgl.opengl.awt.AWTGLCanvas
//import org.lwjgl.opengl.awt.GLData
import java.util.concurrent.atomic.AtomicLong
import javax.swing.SwingUtilities

//class LWJGLCanvas<T: AbstractRenderer>(
//    private val rendererProvider: () -> T
//) : AWTGLCanvas(GLData().apply {
//    swapInterval = 1
//    sRGB = true
//}), IGLObjContainer by IGLObjContainer.Impl() {
//    private val dirtyCounter = AtomicLong(0)
//    private val drawCounter = AtomicLong(0)
//
//    private lateinit var renderer: T
//
//    override fun initGL() {
//        GL.createCapabilities()
//        renderer = rendererProvider()
//        register(renderer)
//    }
//
//    fun redraw() {
//        dirtyCounter.getAndIncrement()
//        SwingUtilities.invokeLater(Runnable {
//            if (!this@LWJGLCanvas.isValid) {
//                GL.setCapabilities(null)
//                return@Runnable
//            }
//            this@LWJGLCanvas.render()
//        })
//    }
//
//    fun update(block: (T) -> Unit) {
//        if (!this::renderer.isInitialized) {
//            SwingUtilities.invokeLater(Runnable {
//                update(block)
//            })
//            return
//        }
//        block(renderer)
//        redraw()
//    }
//
//    override fun paintGL() {
//        val dirty = dirtyCounter.get().toULong()
//        if (drawCounter.get().toULong() >= dirty) {
//            return
//        }
//        drawCounter.set(dirty.toLong())
//        println("Redrawing ${drawCounter.get().toULong()}")
//
//        glViewport(0, 0, framebufferWidth, framebufferHeight)
//        glClearColor(0f, 0f, 0f, 1f)
//        glClear(GL_COLOR_BUFFER_BIT)
//
//        renderer.draw()
//
//        swapBuffers()
//    }
//}