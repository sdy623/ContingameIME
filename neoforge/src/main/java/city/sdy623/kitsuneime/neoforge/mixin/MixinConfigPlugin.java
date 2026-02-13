package city.sdy623.kitsuneime.neoforge.mixin;

import net.neoforged.fml.ModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

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
        // ModList might not be initialized yet during early Mixin loading phase
        ModList modList = ModList.get();
        if (modList == null) {
            // If ModList is not ready yet, apply mixins by default
            // MixinFullScreen will be skipped later if satin is present
            // MixinTextFieldWidget will be skipped if REI is not present
            if (mixinClassName.equals("city.sdy623.kitsuneime.neoforge.mixin.MixinTextFieldWidget")) {
                return false; // Skip REI mixin if we can't check yet
            }
            return true;
        }

        if (mixinClassName.equals("city.sdy623.kitsuneime.neoforge.mixin.MixinFullScreen")) {
            return !modList.isLoaded("satin");
        }
        if (mixinClassName.equals("city.sdy623.kitsuneime.neoforge.mixin.MixinTextFieldWidget")) {
            return modList.isLoaded("roughlyenoughitems");
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
