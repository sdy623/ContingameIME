package city.sdy623.kitsuneime.client.neoforge

import net.minecraft.client.gui.screens.ChatScreen
import java.lang.reflect.Field

object ChatScreenHelperImpl {

    private val initialField: Field by lazy {
        try {
            // Try to find the field using both possible names (obfuscated and deobfuscated)
            val field = ChatScreen::class.java.getDeclaredField("initial")
            field.isAccessible = true
            field
        } catch (e: NoSuchFieldException) {
            // Try SRG/obfuscated name if needed
            // You may need to update this with the correct obfuscated name
            val field = ChatScreen::class.java.getDeclaredField("f_95579_") // Example SRG name
            field.isAccessible = true
            field
        }
    }

    @JvmStatic
    fun getInitial(chatScreen: ChatScreen): String {
        return try {
            initialField.get(chatScreen) as String
        } catch (e: Exception) {
            "" // Return empty string if access fails
        }
    }
}

