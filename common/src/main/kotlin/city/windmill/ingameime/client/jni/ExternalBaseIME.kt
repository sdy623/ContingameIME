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

    private var lastPreEditRectLogAtMs: Long = 0
    private var lastPreEditRectKey: String? = null

    @Volatile
    private var attachSuccessLogged = false

    @Volatile
    private var lastKickAttempt = 0

    fun noteKickAttempt(attempt: Int) {
        if (attempt > lastKickAttempt) lastKickAttempt = attempt
    }

    fun hasAttachedOnce(): Boolean = attachSuccessLogged

    var iCommitListener: ICommitListener = IMEHandler.IMEState

    var State: Boolean = false
        set(value) {
            LOGGER.trace("State $field -> $value")
            field = value
            LOGGER.debug("nSetState(%s)", field)
            nSetState(field)
            OverlayScreen.showAlphaMode = field
        }

    var FullScreen: Boolean = false
        set(value) {
            LOGGER.trace("FullScreen $field -> $value")
            field = value
            LOGGER.debug("nSetFullScreen(%s)", field)
            nSetFullScreen(field)
            if (State) {
                LOGGER.debug("FullScreen changed while State=true; toggling State to refresh context")
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
            val win = Minecraft.getInstance().window
            val hwnd = glfwGetWin32Window(win.window)
            val isFullscreen = win.isFullscreen
            LOGGER.debug("Initializing native IME: hwnd=0x%X fullscreen=%s", hwnd, isFullscreen)
            nInitialize(hwnd)
            FullScreen = isFullscreen
            LOGGER.debug("Native IME initialized: State=%s FullScreen=%s", State, FullScreen)
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
    private external fun nSetPreEditRect(rect: IntArray)

    fun setPreEditRect(rect: IntArray) {
        val now = System.currentTimeMillis()
        val key = if (rect.size >= 4) "${rect[0]},${rect[1]},${rect[2]},${rect[3]}" else rect.joinToString(",")
        if (key != lastPreEditRectKey || (now - lastPreEditRectLogAtMs) > 750) {
            lastPreEditRectKey = key
            lastPreEditRectLogAtMs = now
            LOGGER.debug("nSetPreEditRect([%s])", key)
        }
        nSetPreEditRect(rect)
    }
    //endregion

    //region CallFrom JNI
    @Suppress("unused")
    private fun onCandidateList(candidates: Array<String>?, selectedIndex: Int) {
        val count = candidates?.size ?: 0
        val head = candidates?.firstOrNull()
        LOGGER.debug("onCandidateList(count=%s selected=%s head=%s)", count, selectedIndex, head)
        OverlayScreen.candidates = candidates
        OverlayScreen.selectedCandidateIndex = selectedIndex
    }

    @Suppress("unused")
    private fun onComposition(str: String?, caret: Int, state: CompositionState) {
        LOGGER.debug(
            "onComposition(state=%s caret=%s len=%s)",
            state,
            caret,
            str?.length ?: 0
        )
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
        val ext = OverlayScreen.compositionExt
        if (ext.size >= 4) {
            LOGGER.trace("onGetCompExt -> [%s,%s,%s,%s]", ext[0], ext[1], ext[2], ext[3])
        } else {
            LOGGER.trace("onGetCompExt -> size=%s", ext.size)
        }
        return ext
    }

    @Suppress("unused")
    private fun onAlphaMode(isAlphaMode: Boolean) {
        if (!attachSuccessLogged) {
            attachSuccessLogged = true
            val retries = (lastKickAttempt - 1).coerceAtLeast(0)
            LOGGER.info("[Kitsune] IME Context attached successfully after %s retries.", retries)
        }
        LOGGER.debug("onAlphaMode(%s)", isAlphaMode)
        AlphaMode = isAlphaMode
        OverlayScreen.showAlphaMode = true
    }

    @Suppress("unused")
    private fun onInputLanguage(code: Int) {
        LOGGER.debug("onInputLanguage(code=%s)", code)
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