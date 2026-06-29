package scot.oskar.jing.module

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec
import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentRegistryProxy
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.command.system.AbstractCommand
import scot.oskar.jing.JingPlugin
import scot.oskar.jing.data.AbstractHytaleServiceFactory
import scot.oskar.jing.data.storage.JingPlayerDataProvider
import scot.oskar.jing.module.core.CoreJingModule

abstract class AbstractJingModule (private val basePlugin: JingPlugin) {

    companion object {
        @JvmField
        val CODEC = BuilderCodecMapCodec<AbstractJingModule>("Type", true)

        /**
         * Registers a new [AbstractJingModule] with the [com.hypixel.hytale.codec.lookup.MapKeyMapCodec] for
         * module types
         *
         * @param module the actual [JingPlayerDataProvider]
         * @param codec the Hytale [com.hypixel.hytale.codec.Codec] supplier
         */
        fun <T: AbstractJingModule> registerModule(
            module: T,
            codec: () -> BuilderCodec<T>
        ) {
            CODEC.register(module::class.simpleName!!, module::class.java, codec())
        }
    }

    protected val logger: HytaleLogger = HytaleLogger.forEnclosingClass()

    /**
     * Main entrypoint for the module, gets called once all modules are registered and ready to be started
     */
    abstract fun load()

    /**
     * Shuts down the module, called on plugin shutdown
     */
    abstract fun shutdown()

    /**
     * Registers the [commands] array of [AbstractCommand] for the given module
     */
    fun registerCommands(vararg commands: AbstractCommand) =
        commands.forEach { basePlugin.commandRegistry.registerCommand(it) }

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

class ModuleService private constructor(val plugin: JingPlugin) {

    val logger = HytaleLogger.forEnclosingClass()

    companion object Factory : AbstractHytaleServiceFactory<ModuleService>() {
        override fun create(plugin: JingPlugin): ModuleService = ModuleService(plugin)
    }

    init {
        AbstractJingModule.registerModule(CoreJingModule(plugin)) { CoreJingModule.CODEC }
    }

    fun loadModules() {
        val enabledModules = plugin.pluginConfig.get().enabledModules
        if(enabledModules.isEmpty()) {
            logger.atSevere().log("There are no modules provided to load!")
            return
        }
        logger.atInfo().log("Loading ${enabledModules.count()} modules...")
        enabledModules.forEach {
            logger.atInfo().log("Loading ${it::class.simpleName}..")
            it.load()
        }
    }
}