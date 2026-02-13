package city.sdy623.kitsuneime.neoforge.mixin;

import city.sdy623.kitsuneime.client.event.ClientScreenEventHooks;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
class MixinFullScreen {
    @Final
    @Shadow
    private Window window;

    @Inject(method = "resizeDisplay", at = @At("TAIL"))
    private void onScreenSizeChanged(CallbackInfo ci) {
        ClientScreenEventHooks.INSTANCE.getWINDOW_SIZE_CHANGED().invoker().onWindowSizeChanged(window.getWidth(),
                window.getHeight());
    }
}
