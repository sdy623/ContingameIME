package city.sdy623.kitsuneime.fabric.mixin;

import city.sdy623.kitsuneime.client.event.ClientScreenEventHooks;
import city.sdy623.kitsuneime.client.gui.CaretPositionHelper;
import kotlin.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignEditScreen.class)
abstract class MixinSignEditScreen {
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(0, 0));
    }

    @Redirect(method = "renderSignText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", ordinal = 1))
    private int onCaretDraw(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color, boolean shadow) {
        int result = guiGraphics.drawString(font, text, x, y, color, shadow);
        if ("_".equals(text)) {
            CaretPositionHelper.emitCaretFromGui(this, guiGraphics, x, y);
        }
        return result;
    }

    @Redirect(method = "renderSignText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private void onCaretFill(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        guiGraphics.fill(x1, y1, x2, y2, color);
        CaretPositionHelper.emitCaretFromGui(this, guiGraphics, x1, y1);
    }
}
