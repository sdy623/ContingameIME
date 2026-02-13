package city.sdy623.kitsuneime

import city.sdy623.kitsuneime.client.event.ClientScreenEventHooks
import city.sdy623.kitsuneime.client.gui.OverlayScreen
import city.sdy623.kitsuneime.client.handler.ConfigHandler
import city.sdy623.kitsuneime.client.handler.IMEHandler
import city.sdy623.kitsuneime.client.handler.KeyHandler
import city.sdy623.kitsuneime.client.handler.ScreenHandler
import city.sdy623.kitsuneime.client.jni.ExternalBaseIME
import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientScreenInputEvent
import dev.architectury.platform.Platform
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory

object KitsuneIMEClient {
    const val MODNAME = "ContingameIME-Neo"
    const val MODID = "ingameime"
    val LOGGER = LoggerFactory.getLogger(MODNAME)
    /**
     * Track mouse move
     */
    private var prevX = 0
    private var prevY = 0
    
    /**
     * Flag to ensure IME is initialized once on first frame render
     * This is the most robust approach: by the time the first frame renders,
     * GLFW window creation is complete and Windows message loop is fully initialized
     */
    private var firstFrameRendered = false

    /**
     * Second safety net (FocusHook): kick IME again on first real user interaction
     * (mouse move / key press). This covers cases where the first-frame kick still
     * races with late focus/input-mode adjustments.
     */
    private var interactionKicked = false

    /**
     * Third safety net (Retry Loop): a few delayed kicks after first frame.
     */
    private var imeKickAttemptsRemaining = 0
    private var nextImeKickAtMs = 0L

    private var kickAttempt = 0

    private fun kickIme(reason: String) {
        try {
            if (ExternalBaseIME.hasAttachedOnce()) return

            kickAttempt += 1
            ExternalBaseIME.noteKickAttempt(kickAttempt)

            val window = Minecraft.getInstance().window
            ExternalBaseIME.FullScreen = ConfigHandler.shouldUseFullscreenCandidate(window.isFullscreen)
            ExternalBaseIME.setPreEditRect(OverlayScreen.compositionExt)

            ExternalBaseIME.State = true
            ExternalBaseIME.State = false

            // Failures/retries stay DEBUG-only; success is logged once from ExternalBaseIME.onAlphaMode.
            LOGGER.debug("IME kick attempt #{} ({})", kickAttempt, reason)
        } catch (t: Throwable) {
            LOGGER.warn("IME kick failed: {}", reason, t)
        }
    }

    private fun scheduleImeRetryLoop(attempts: Int, delayMs: Long) {
        imeKickAttemptsRemaining = attempts
        nextImeKickAtMs = System.currentTimeMillis() + delayMs
    }

    fun registerConfigScreen() {
        Platform.getMod(MODID).registerConfigurationScreen { parent ->
            ConfigHandler.createConfigScreen().setParentScreen(parent).build()
        }
    }

    fun onInitClient() {
        ConfigHandler.initialConfig()
        ClientGuiEvent.RENDER_POST.register(ClientGuiEvent.ScreenRenderPost { _, matrices, mouseX, mouseY, delta ->
            // Ensure IME is initialized on first frame render
            // By this point, GLFW window is fully created and Windows message loop is ready
            if (!firstFrameRendered) {
                firstFrameRendered = true
                kickIme("first-frame")
                // A few retries in case something (GLFW/Windows focus/input-mode) changes right after.
                scheduleImeRetryLoop(attempts = 3, delayMs = 250)
            } else if (imeKickAttemptsRemaining > 0 && !ExternalBaseIME.hasAttachedOnce()) {
                val now = System.currentTimeMillis()
                if (now >= nextImeKickAtMs) {
                    imeKickAttemptsRemaining -= 1
                    kickIme("retry-${imeKickAttemptsRemaining}")
                    nextImeKickAtMs = now + 500
                }
            } else if (imeKickAttemptsRemaining > 0 && ExternalBaseIME.hasAttachedOnce()) {
                imeKickAttemptsRemaining = 0
            }
            
            //Track mouse move here
            if (mouseX != prevX || mouseY != prevY) {
                if (!interactionKicked && firstFrameRendered && !ExternalBaseIME.hasAttachedOnce()) {
                    interactionKicked = true
                    kickIme("interaction-mouse-move")
                }
                ClientScreenEventHooks.SCREEN_MOUSE_MOVE.invoker().onMouseMove(prevX, prevY, mouseX, mouseY)

                prevX = mouseX
                prevY = mouseY
            }

            OverlayScreen.render(matrices, mouseX, mouseY, delta.gameTimeDeltaTicks)
        })
        ClientScreenEventHooks.SCREEN_MOUSE_MOVE.register(ClientScreenEventHooks.MouseMove { _, _, _, _ ->
            IMEHandler.IMEState.onMouseMove()
        })
        ClientScreenInputEvent.KEY_PRESSED_PRE.register(ClientScreenInputEvent.KeyPressed { _, _, keyCode, scanCode, modifiers ->
            if (!interactionKicked && firstFrameRendered && !ExternalBaseIME.hasAttachedOnce()) {
                interactionKicked = true
                kickIme("interaction-key-press")
            }
            if (KeyHandler.KeyState.onKeyDown(keyCode, scanCode, modifiers))
                EventResult.interruptDefault()
            else
                EventResult.pass()
        })
        ClientScreenInputEvent.KEY_RELEASED_PRE.register(ClientScreenInputEvent.KeyReleased { _, _, keyCode, scanCode, modifiers ->
            if (KeyHandler.KeyState.onKeyUp(keyCode, scanCode, modifiers))
                EventResult.interruptDefault()
            else
                EventResult.pass()
        })
        ClientScreenEventHooks.WINDOW_SIZE_CHANGED.register(ClientScreenEventHooks.WindowSizeChanged { _, _ ->
            ExternalBaseIME.FullScreen = ConfigHandler.shouldUseFullscreenCandidate(Minecraft.getInstance().window.isFullscreen)
        })
        with(ScreenHandler.ScreenState) {
            ClientScreenEventHooks.SCREEN_CHANGED.register(ClientScreenEventHooks.ScreenChanged(ScreenHandler.ScreenState.Companion::onScreenChange))
        }
        with(ScreenHandler.ScreenState.EditState) {
            ClientScreenEventHooks.EDIT_OPEN.register(ClientScreenEventHooks.EditOpen(ScreenHandler.ScreenState.EditState.Companion::onEditOpen))
            ClientScreenEventHooks.EDIT_CARET.register(ClientScreenEventHooks.EditCaret(ScreenHandler.ScreenState.EditState.Companion::onEditCaret))
            ClientScreenEventHooks.EDIT_CLOSE.register(ClientScreenEventHooks.EditClose(ScreenHandler.ScreenState.EditState.Companion::onEditClose))
        }
    }
}