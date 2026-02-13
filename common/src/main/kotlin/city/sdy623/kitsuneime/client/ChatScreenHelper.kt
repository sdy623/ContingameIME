package city.sdy623.kitsuneime.client

import dev.architectury.injectables.annotations.ExpectPlatform
import net.minecraft.client.gui.screens.ChatScreen

object ChatScreenHelper {

    @ExpectPlatform
    @JvmStatic
    fun getInitial(chatScreen: ChatScreen): String {
        throw AssertionError("This method should be replaced by Architectury")
    }
}

