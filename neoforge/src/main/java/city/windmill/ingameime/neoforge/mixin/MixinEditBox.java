package city.windmill.ingameime.neoforge.mixin;

import city.windmill.ingameime.client.event.ClientScreenEventHooks;
import kotlin.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EditBox.class)
abstract class MixinEditBox extends AbstractWidget {
    @Shadow
    private boolean bordered;

    @Shadow
    private boolean isEditable;

    @Shadow
    private int displayPos;

    @Shadow
    private Font font;

    private MixinEditBox(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
    }

    @Inject(method = "setFocused", at = @At("HEAD"))
    private void onSelected(boolean selected, CallbackInfo info) {
        int y = this.getY();
        int caretX = computeCaretX();
        int caretY = bordered ? y + (height - 8) / 2 : y;
        if (selected && isEditable)
            ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(caretX, caretY));
        else
            ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
    }

    @Inject(method = "setEditable", at = @At("HEAD"))
    private void onEditableChange(boolean bl, CallbackInfo ci) {
        int y = this.getY();
        int caretX = computeCaretX();
        int caretY = bordered ? y + (height - 8) / 2 : y;
        if (!bl)
            ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
        else if (isFocused())
            ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(caretX, caretY));
    }

    @Inject(method = "onClick", at = @At(value = "INVOKE", target = "net/minecraft/util/Mth.floor(D)I", shift = At.Shift.BEFORE, ordinal = 0))
    private void onFocused(double d, double e, CallbackInfo ci) {
        int y = this.getY();
        int caretX = computeCaretX();
        int caretY = bordered ? y + (height - 8) / 2 : y;
        if (isFocused() && isEditable)
            ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(caretX, caretY));
        else
            ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
    }

    @Inject(method = "setCursorPosition", at = @At("TAIL"))
    private void onCursorPositionChanged(int pos, CallbackInfo ci) {
        updateCaretPosition();
    }

    @Inject(method = "setValue", at = @At("TAIL"))
    private void onValueChanged(String value, CallbackInfo ci) {
        updateCaretPosition();
    }

    @ModifyArgs(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private void onCaretFill(Args args) {
        int x1 = args.get(0);
        int y1 = args.get(1);
        int x2 = args.get(2);
        int y2 = args.get(3);
        if (x2 - x1 <= 1 && y2 - y1 >= 1) {
            ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(x1, y1));
        }
    }

    private int computeCaretX() {
        int baseX = bordered ? this.getX() + 4 : this.getX();
        EditBox self = (EditBox) (Object) this;
        String value = self.getValue();
        int cursor = self.getCursorPosition();
        if (value == null || value.isEmpty() || cursor <= 0) {
            return baseX;
        }
        int start = Math.max(0, Math.min(displayPos, value.length()));
        int end = Math.max(start, Math.min(cursor, value.length()));
        return baseX + font.width(value.substring(start, end));
    }

    private void updateCaretPosition() {
        int y = this.getY();
        int caretX = computeCaretX();
        int caretY = bordered ? y + (height - 8) / 2 : y;
        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(caretX, caretY));
    }
}
