package city.windmill.ingameime.neoforge

import city.windmill.ingameime.IngameIMEClient
import city.windmill.ingameime.client.handler.KeyHandler
import net.minecraft.Util
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod("ingameime")
@EventBusSubscriber(modid = "ingameime", bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
object IngameIMEClientNeoForge {
    
    init {
        IngameIMEClient.registerConfigScreen()
        
        if (Util.getPlatform() == Util.OS.WINDOWS) {
            IngameIMEClient.LOGGER.info("it is Windows OS! Loading mod...")
            
            MOD_BUS.addListener(::onClientSetup)
            MOD_BUS.addListener(::onRegisterKeyMappings)
        } else {
            IngameIMEClient.LOGGER.warn("This mod cant work in ${Util.getPlatform()} !")
        }
    }
    
    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            IngameIMEClient.onInitClient()
        }
    }
    
    @JvmStatic
    @SubscribeEvent
    fun onRegisterKeyMappings(event: RegisterKeyMappingsEvent) {
        event.register(KeyHandler.toggleKey)
    }
}
