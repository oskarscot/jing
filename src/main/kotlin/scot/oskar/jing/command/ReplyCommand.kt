package scot.oskar.jing.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import scot.oskar.jing.component.PrivateMessageComponent
import scot.oskar.jing.data.PlayerId

class ReplyCommand : AbstractPlayerCommand("reply", "Replies to the message") {

    val messageArg = withRequiredArg("message", "The contents of the message", ArgTypes.STRING)

    init {
        addAliases("r")
    }

    //TODO: i18n for the messages along with message templating
    override fun execute(context: CommandContext, store: Store<EntityStore>, ref: Ref<EntityStore>, player: PlayerRef, world: World) {
        val message = context.get(messageArg)
        val senderComponent = store.ensureAndGetComponent(ref, PrivateMessageComponent.COMPONENT_TYPE)
        val lastReply = senderComponent.lastMessage ?: run {
            player.sendMessage(Message.raw("You don't have anyone to reply to."))
            return
        }
        val targetPlayer = Universe.get().getPlayer(lastReply.id) ?: run {
            senderComponent.lastMessage = null
            player.sendMessage(Message.raw("This player is no longer online."))
            return
        }
        val targetComponent = store.ensureAndGetComponent(targetPlayer.reference!!, PrivateMessageComponent.COMPONENT_TYPE)
        targetComponent.lastMessage = PlayerId(player.uuid)
        targetPlayer.sendMessage(Message.raw("${player.username} -> You: $message"))
        player.sendMessage(Message.raw("Replying to ${targetPlayer.username}: $message"))
    }

    override fun canGeneratePermission(): Boolean = false
}