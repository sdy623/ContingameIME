package city.sdy623.kitsuneime.client.jni

import city.sdy623.kitsuneime.KitsuneIMEClient
import net.minecraft.server.packs.resources.Resource
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object NativeLoader {
    private val LOGGER = LogManager.getFormatterLogger(KitsuneIMEClient.MODNAME + "|NativeLoader")!!
    
    /**
     * Loads library from minecraft Resource
     */
    fun load(lib: Resource) {
        LOGGER.debug("Try load native from ${lib.sourcePackId()}")
        val tempFile = Files.createTempFile("kitsuneime-Native", null).apply {
            LOGGER.debug("Copying Native to {}", this)
            Files.copy(lib.open(), this, StandardCopyOption.REPLACE_EXISTING)
        }
        System.load(tempFile.toAbsolutePath().toString())
    }
}