package city.windmill.ingameime.neoforge.mixin

import net.minecraft.client.KeyboardHandler
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Invoker

@Mixin(KeyboardHandler::class)
interface MixinKeyboardHandler {

    @Invoker("charTyped")
    fun invokeCharTyped(window: Long, codePoint: Int, modifiers: Int)
}


