package city.windmill.ingameime.client.fabric

import city.windmill.ingameime.fabric.mixin.MixinKeyboardHandler
import net.minecraft.client.KeyboardHandler

object KeyboardHelperImpl {

    @JvmStatic
    fun sendCharTyped(handler: KeyboardHandler, window: Long, codePoint: Int, modifiers: Int) {
        (handler as MixinKeyboardHandler).invokeCharTyped(window, codePoint, modifiers)
    }
}
