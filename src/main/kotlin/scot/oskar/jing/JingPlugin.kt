package scot.oskar.jing

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.util.Config
import kotlinx.coroutines.*
import scot.oskar.jing.data.JingPlayerData
import scot.oskar.jing.data.PlayerId
import scot.oskar.jing.data.storage.JingPlayerDataProvider.Companion.registerDataProvider
import scot.oskar.jing.data.storage.SimplePlayerDataProvider

class JingPlugin(init: JavaPluginInit): JavaPlugin(init) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val config: Config<JingConfiguration>

    init {
        SimplePlayerDataProvider.basePath = dataDirectory

        registerDataProvider(provider = SimplePlayerDataProvider()) { SimplePlayerDataProvider.CODEC }

        config = withConfig(JingConfigurationCodec)
    }

    override fun setup() {
        logger.atInfo().log("Using ${config.get().storageProvider::class.simpleName} as storage provider.")

        eventRegistry.registerGlobal(PlayerConnectEvent::class.java) { event ->
            val uuid = event.playerRef.uuid
            scope.launch {
                val saved = config.get().storageProvider.getOrCreate(PlayerId(uuid)) {
                    logger.atInfo().log("Registering new player: ${PlayerId(uuid)}")
                    event.playerRef.sendMessage(Message.raw("Welcome for the first time!"))
                    JingPlayerData(uuid.toString())
                }
                event.playerRef.sendMessage(Message.raw("Stored UUID: ${saved.test}"))
            }
        }

        config.save()
    }

    override fun shutdown() {
        scope.cancel()
    }
}