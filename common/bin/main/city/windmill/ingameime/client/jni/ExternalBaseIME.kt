package city.windmill.ingameime.client.jni

import city.windmill.ingameime.IngameIMEClient
import city.windmill.ingameime.client.KeyboardHelper
import city.windmill.ingameime.client.handler.IMEHandler
import city.windmill.ingameime.client.gui.OverlayScreen
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.util.Locale

fun interface ICommitListener {
    fun onCommit(commit: String): String
}

object ExternalBaseIME {
    private val LOGGER = LogManager.getFormatterLogger(IngameIMEClient.MODNAME + "|ExternalBaseIME")!!

    var iCommitListener: ICommitListener = IMEHandler.IMEState

    var State: Boolean = false
        set(value) {
            LOGGER.trace("State $field -> $value")
            field = value
            nSetState(field)
            OverlayScreen.showAlphaMode = field
        }

    var FullScreen: Boolean = false
        set(value) {
            LOGGER.trace("FullScreen $field -> $value")
            field = value
            nSetFullScreen(field)
            if (State) {
                State = false
                State = true
            }
        }

    var AlphaMode: Boolean = false
        private set(value) {
            LOGGER.trace("AlphaMode $field -> $value")
            field = value
        }

    var InputLanguage: InputLang = InputLang.OTHER
        private set(value) {
            if (field == value) return
            LOGGER.trace("InputLanguage $field -> $value")
            field = value
        }

    init {
        try {
            /*  不知为何 Minecraft.getInstance().is64Bit 属性无法被解析。
                val x86 = if (Minecraft.getInstance().is64Bit) "" else "-x86"
                val resourceNative = ResourceLocation("ingameime", "natives/jni$x86.dll")
            */
            val resourceNative = ResourceLocation.fromNamespaceAndPath("ingameime", "natives/jni.dll")
            NativeLoader.load(Minecraft.getInstance().resourceManager.getResource(resourceNative).orElseThrow())
            LOGGER.debug("Initialing window")
            nInitialize(glfwGetWin32Window(Minecraft.getInstance().window.window))
            FullScreen = Minecraft.getInstance().window.isFullscreen
        } catch (ex: Exception) {
            LOGGER.error("Failed in initializing ExternalBaseIME:", ex)
        }
    }

    //region Natives
    private external fun nInitialize(handle: Long)

    @Suppress("unused")
    private external fun nUninitialize()
    private external fun nSetState(state: Boolean)
    private external fun nSetFullScreen(fullscreen: Boolean)
    //endregion

    //region CallFrom JNI
    @Suppress("unused")
    private fun onCandidateList(candidates: Array<String>?, selectedIndex: Int) {
        OverlayScreen.candidates = candidates
        OverlayScreen.selectedCandidateIndex = selectedIndex
    }

    @Suppress("unused")
    private fun onComposition(str: String?, caret: Int, state: CompositionState) {
        when (state) {
            CompositionState.Commit -> {
                OverlayScreen.composition = null
                iCommitListener.onCommit(str!!).onEach { ch ->
                    val handler = Minecraft.getInstance().keyboardHandler
                    KeyboardHelper.sendCharTyped(handler, Minecraft.getInstance().window.window, ch.code, 0)
                }
            }
            CompositionState.Start,
            CompositionState.End,
            CompositionState.Update -> {
                OverlayScreen.composition = if (str.isNullOrEmpty()) null else str to caret
            }
        }
        OverlayScreen.showAlphaMode = false
    }

    @Suppress("unused")
    private fun onGetCompExt(): IntArray {
        return OverlayScreen.compositionExt
    }

    @Suppress("unused")
    private fun onAlphaMode(isAlphaMode: Boolean) {
        AlphaMode = isAlphaMode
        OverlayScreen.showAlphaMode = true
    }

    @Suppress("unused")
    private fun onInputLanguage(code: Int) {
        InputLanguage = InputLang.fromCode(code)
        OverlayScreen.showAlphaMode = true
    }
    //endregion

    fun nativeModeLabel(): String {
        val uiLang = Minecraft.getInstance().options.languageCode
            .lowercase(Locale.ROOT)
        val useCjkStyle = uiLang.startsWith("zh") || uiLang.startsWith("ja") || uiLang.startsWith("ko")

        if (InputLanguage == InputLang.ENGLISH || InputLanguage == InputLang.OTHER) {
            return if (isCapsLockOn()) "A" else if (useCjkStyle) "英" else "EN"
        }

        val alphaLabel = if (useCjkStyle) "A" else "EN"
        if (AlphaMode) return alphaLabel

        return when (InputLanguage) {
            InputLang.CHINESE -> if (useCjkStyle) "中" else "ZH"
            InputLang.JAPANESE -> if (useCjkStyle) "日" else "JP"
            InputLang.ENGLISH -> if (useCjkStyle) "英" else "EN"
            InputLang.OTHER -> if (useCjkStyle) "A" else "IME"
        }
    }

    private fun isCapsLockOn(): Boolean {
        return try {
            Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK)
        } catch (ex: Exception) {
            false
        }
    }

    private enum class CompositionState {
        Start,
        Update,
        End,
        Commit,
    }

    enum class InputLang(val code: Int) {
        OTHER(0),
        CHINESE(1),
        JAPANESE(2),
        ENGLISH(3);

        companion object {
            fun fromCode(code: Int): InputLang {
                return values().firstOrNull { it.code == code } ?: OTHER
            }
        }
    }
}