package scot.oskar.jing

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentRegistryProxy
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.AbstractCommand
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.util.Config
import kotlinx.coroutines.*
import scot.oskar.jing.command.PrivateMessageCommand
import scot.oskar.jing.command.ReplyCommand
import scot.oskar.jing.component.PrivateMessageComponent
import scot.oskar.jing.config.JingPluginConfiguration
import scot.oskar.jing.config.JingPluginConfigurationCodec
import scot.oskar.jing.config.lang.I18n
import scot.oskar.jing.config.lang.I18nService
import scot.oskar.jing.data.JingPlayerData
import scot.oskar.jing.data.PlayerId
import scot.oskar.jing.data.storage.JingPlayerDataProvider.Companion.registerDataProvider
import scot.oskar.jing.data.storage.SimplePlayerDataProvider
import scot.oskar.jing.ext.sendMessage

class JingPlugin(init: JavaPluginInit): JavaPlugin(init) {

    companion object {
        val SCOPE = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    private val pluginConfig: Config<JingPluginConfiguration>
    private val i18nService =  I18nService(dataDirectory)

    init {
        SimplePlayerDataProvider.basePath = dataDirectory

        registerDataProvider(provider = SimplePlayerDataProvider()) { SimplePlayerDataProvider.CODEC }

        pluginConfig = withConfig(JingPluginConfigurationCodec)
    }

    fun registerComponents() {
        PrivateMessageComponent.COMPONENT_TYPE = registerNonSerializableComponent(entityStoreRegistry) { PrivateMessageComponent() }
    }

    fun registerEvents() {
        eventRegistry.registerGlobal(PlayerConnectEvent::class.java) { event ->
            val uuid = event.playerRef.uuid
            SCOPE.launch {
                val saved = pluginConfig.get().storageProvider.getOrCreate(PlayerId(uuid)) {
                    logger.atInfo().log("Registering new player: ${PlayerId(uuid)}")
                    event.playerRef.sendMessage(Message.raw("Welcome for the first time!"))
                    JingPlayerData(uuid.toString())
                }
                event.playerRef.sendMessage(Message.raw("Stored UUID: ${saved.test}"))
            }

            event.playerRef.sendMessage("message.welcome", "username" to event.playerRef.username)
            event.playerRef.sendMessage("message.invalid", "username" to event.playerRef.username)
        }
    }

    override fun setup() {
        logger.atInfo().log("Using ${pluginConfig.get().storageProvider::class.simpleName} as storage provider.")

        i18nService.load()
        I18n.init(i18nService)

        registerComponents()
        registerEvents()
        registerCommands(
            PrivateMessageCommand(),
            ReplyCommand()
        )

        pluginConfig.save()
    }

    override fun shutdown() {
        SCOPE.cancel()
    }

    fun registerCommands(vararg commands: AbstractCommand) =
        commands.forEach { commandRegistry.registerCommand(it) }

    /**
     *  Fancy typed helper function for registering a non-serializable [Component] for the provided ECS Store type
     *
     *  @param store the [ComponentRegistryProxy] typed for the [ECS_STORE]
     *  @param component the [java.util.function.Supplier] for creating component instances
     */
    inline fun <ECS_STORE, reified T : Component<ECS_STORE>> registerNonSerializableComponent(
        store: ComponentRegistryProxy<ECS_STORE>,
        noinline component: () -> T) : ComponentType<ECS_STORE, T> {
        return store.registerComponent(T::class.java , component)
    }

    /**
     *  Fancy typed helper function for registering a serializable [Component] for the provided ECS Store type
     *
     *  @param store the [ComponentRegistryProxy] typed for the [ECS_STORE]
     *  @param id component ID to be used for serialization
     *  @param codec the Hytale [BuilderCodec] to use for component serialization
     */
    inline fun <ECS_STORE, reified T : Component<ECS_STORE>> registerSerializableComponent(
        store: ComponentRegistryProxy<ECS_STORE>,
        id: String = T::class.java.name,
        codec: () -> BuilderCodec<T>
    ) : ComponentType<ECS_STORE, T> {
        return store.registerComponent(T::class.java , id, codec())
    }
}