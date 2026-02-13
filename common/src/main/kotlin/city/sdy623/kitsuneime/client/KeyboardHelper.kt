package city.sdy623.kitsuneime.client

import dev.architectury.injectables.annotations.ExpectPlatform
import net.minecraft.client.KeyboardHandler

object KeyboardHelper {

    @ExpectPlatform
    @JvmStatic
    fun sendCharTyped(handler: KeyboardHandler, window: Long, codePoint: Int, modifiers: Int) {
        throw AssertionError("This method should be replaced by Architectury")
    }
}

