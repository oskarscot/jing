package scot.oskar.jing.module.core

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import kotlinx.coroutines.launch
import scot.oskar.jing.JingPlugin
import scot.oskar.jing.JingPlugin.Companion.SCOPE
import scot.oskar.jing.module.core.command.PrivateMessageCommand
import scot.oskar.jing.module.core.command.ReplyCommand
import scot.oskar.jing.component.PrivateMessageComponent
import scot.oskar.jing.data.JingPlayerData
import scot.oskar.jing.data.PlayerId
import scot.oskar.jing.ext.sendMessage
import scot.oskar.jing.module.AbstractJingModule
import scot.oskar.jing.module.core.command.HomeCommand
import scot.oskar.jing.module.core.command.HomeDeleteCommand
import scot.oskar.jing.module.core.command.HomeSetCommand

class CoreJingModule(private val basePlugin: JingPlugin): AbstractJingModule(basePlugin) {

    companion object {
        val CODEC = BuilderCodec.abstractBuilder(CoreJingModule::class.java).build()
    }

    override fun load() {
        registerComponents()

        registerCommands(
            ReplyCommand(),
            PrivateMessageCommand(),
            HomeCommand(),
            HomeSetCommand(),
            HomeDeleteCommand()
        )

        registerEvents()
    }

    override fun shutdown() {
        TODO("Not yet implemented")
    }

    fun registerComponents() {
        PrivateMessageComponent.COMPONENT_TYPE = registerNonSerializableComponent(basePlugin.entityStoreRegistry) { PrivateMessageComponent() }
    }

    fun registerEvents() {
        basePlugin.eventRegistry.registerGlobal(PlayerConnectEvent::class.java) { event ->
            val player = event.playerRef
            SCOPE.launch {
                basePlugin.pluginConfig.get().storageProvider.getOrCreate(PlayerId(player.uuid)) {
                    logger.atInfo().log("Registering new player: ${PlayerId(player.uuid)}")
                    event.playerRef.sendMessage(Message.raw("Welcome for the first time!"))
                    JingPlayerData()
                }
            }

            player.sendMessage("message.welcome", "username" to player.username)
            player.sendMessage("message.invalid", "username" to player.username)
        }
    }

}