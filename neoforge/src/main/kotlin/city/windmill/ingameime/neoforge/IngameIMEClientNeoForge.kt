package city.windmill.ingameime.neoforge

import city.windmill.ingameime.IngameIMEClient
import city.windmill.ingameime.client.handler.KeyHandler
import net.minecraft.Util
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_CONTEXT

@Mod("ingameime")
object IngameIMEClientNeoForge {

    init {
        IngameIMEClient.registerConfigScreen()
        
        if (Util.getPlatform() == Util.OS.WINDOWS) {
            IngameIMEClient.LOGGER.info("it is Windows OS! Loading mod...")
            
            val modBus = MOD_CONTEXT.getKEventBus()
            modBus.addListener(::onClientSetup)
            modBus.addListener(::onRegisterKeyMappings)
        } else {
            IngameIMEClient.LOGGER.warn("This mod cant work in ${Util.getPlatform()} !")
        }
    }
    
    @Suppress("UNUSED_PARAMETER")
    private fun onClientSetup(event: FMLClientSetupEvent) {
        IngameIMEClient.onInitClient()
    }
    
    private fun onRegisterKeyMappings(event: RegisterKeyMappingsEvent) {
        event.register(KeyHandler.toggleKey)
    }
}
