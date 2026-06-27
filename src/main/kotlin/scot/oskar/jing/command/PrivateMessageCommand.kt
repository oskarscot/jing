package scot.oskar.jing.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import scot.oskar.jing.component.PrivateMessageComponent
import scot.oskar.jing.data.PlayerId

class PrivateMessageCommand: AbstractPlayerCommand("msg", "Privately messages someone") {

    val playerArg = withRequiredArg("target", "Target player", ArgTypes.PLAYER_REF)
    val messageArg = withRequiredArg("message", "The contents of the message", ArgTypes.STRING)

    override fun execute(
        context: CommandContext,
        store: Store<EntityStore>,
        ref: Ref<EntityStore>,
        player: PlayerRef,
        world: World
    ) {
        val target = context.get(playerArg)
        val message = context.get(messageArg)

        val component = store.ensureAndGetComponent(target.reference!!, PrivateMessageComponent.COMPONENT_TYPE)
        component.lastMessage = PlayerId(player.uuid)
        //TODO: i18n for the messages along with message templating
        target.sendMessage(Message.raw("${player.username} -> You: $message"))
        player.sendMessage(Message.raw("You -> ${player.username}: $message"))
    }

    override fun canGeneratePermission(): Boolean = false
}