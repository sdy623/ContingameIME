package city.sdy623.kitsuneime.fabric

import city.sdy623.kitsuneime.KitsuneIMEClient
import city.sdy623.kitsuneime.client.handler.KeyHandler
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.Util

@Environment(EnvType.CLIENT)
object KitsuneIMEClientFabric : ClientModInitializer {

    override fun onInitializeClient() {
        KitsuneIMEClient.registerConfigScreen()
        if (Util.getPlatform() == Util.OS.WINDOWS) {
            KitsuneIMEClient.LOGGER.info("it is Windows OS! Loading mod...")

            ClientLifecycleEvents.CLIENT_STARTED.register(ClientLifecycleEvents.ClientStarted {
                KitsuneIMEClient.onInitClient()
            })
            KeyBindingHelper.registerKeyBinding(KeyHandler.toggleKey)
        } else
            KitsuneIMEClient.LOGGER.warn("This mod cant work in ${Util.getPlatform()} !")
    }
}