package city.sdy623.kitsuneime.mixin;

import dev.architectury.platform.Platform;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Common Mixin configuration plugin using Architectury for cross-loader
 * compatibility.
 * Handles conditional loading of mixins based on other mods.
 */
public class MixinConfigPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Skip MixinFullScreen if satin mod is loaded (to avoid conflicts)
        if (mixinClassName.equals("city.sdy623.kitsuneime.mixin.MixinFullScreen")) {
            try {
                return !Platform.isModLoaded("satin");
            } catch (Exception e) {
                // If Platform is not initialized yet, apply the mixin by default
                // (satin is rarely used anyway)
                return true;
            }
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
