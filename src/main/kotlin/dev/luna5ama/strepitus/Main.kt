package dev.luna5ama.strepitus

import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.scene.*
import androidx.compose.ui.unit.*
import dev.luna5ama.strepitus.gl.GlfwCoroutineDispatcher
import dev.luna5ama.strepitus.gl.subscribeToGLFWEvents
import org.jetbrains.skia.*
import org.jetbrains.skia.Color
import org.jetbrains.skiko.FrameDispatcher
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL

@OptIn(InternalComposeUiApi::class)
fun main() {
    var width = 1920
    var height = 1080

    glfwInit()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
    val windowHandle: Long = glfwCreateWindow(width, height, "Strepitus", 0L, 0L)
    glfwMakeContextCurrent(windowHandle)
    glfwSwapInterval(1)

    GL.createCapabilities()

    val context = DirectContext.makeGL()
    var renderTarget = BackendRenderTarget.makeGL(width, height, 1, 8, 0, 0x8058)
    var surface = Surface.makeFromBackendRenderTarget(
        context,
        renderTarget,
        SurfaceOrigin.BOTTOM_LEFT,
        SurfaceColorFormat.RGBA_8888,
        ColorSpace.sRGB,
        SurfaceProps()
    )!!
    val glfwDispatcher = GlfwCoroutineDispatcher() // a custom coroutine dispatcher, in which Compose will run

    glfwSetWindowCloseCallback(windowHandle) {
        glfwDispatcher.stop()
    }

    lateinit var composeScene: ComposeScene

    fun render() {
        surface.canvas.clear(Color.WHITE)
        composeScene.size = IntSize(width, height)
        composeScene.render(surface.canvas.asComposeCanvas(), System.nanoTime())

        context.flush()
        glfwSwapBuffers(windowHandle)
    }

    val frameDispatcher = FrameDispatcher(glfwDispatcher) { render() }

//    glfwGetWindowContentScale(windowHandle)
    val density = Density(1.0f)
    composeScene = CanvasLayersComposeScene(
        density,
        invalidate = frameDispatcher::scheduleFrame,
        coroutineContext = glfwDispatcher
    )

    glfwSetWindowSizeCallback(windowHandle) { _, windowWidth, windowHeight ->
        width = windowWidth
        height = windowHeight
        surface.close()
        renderTarget.close()

        renderTarget = BackendRenderTarget.makeGL(width, height, 1, 8, 0, 0x8058)
        surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB,
            SurfaceProps()
        )!!


        glfwSwapInterval(0)
        render()
        glfwSwapInterval(1)
    }

    composeScene.subscribeToGLFWEvents(windowHandle)
    composeScene.setContent { App() }
    glfwShowWindow(windowHandle)

    glfwDispatcher.runLoop()

    composeScene.close()
    glfwDestroyWindow(windowHandle)
}