package scot.oskar.jing.module.core.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import scot.oskar.jing.component.PrivateMessageComponent
import scot.oskar.jing.data.PlayerId
import scot.oskar.jing.ext.sendMessage

class ReplyCommand : AbstractPlayerCommand("reply", "Replies to the message") {

    val messageArg = withRequiredArg("message", "The contents of the message", ArgTypes.STRING)

    init {
        addAliases("r")
    }

    override fun execute(context: CommandContext, store: Store<EntityStore>, ref: Ref<EntityStore>, player: PlayerRef, world: World) {
        val message = context.get(messageArg)
        val senderComponent = store.ensureAndGetComponent(ref, PrivateMessageComponent.COMPONENT_TYPE)
        val lastReply = senderComponent.lastMessage ?: run {
            player.sendMessage("message.pm.noTarget")
            return
        }
        val target = Universe.get().getPlayer(lastReply.id) ?: run {
            senderComponent.lastMessage = null
            player.sendMessage("message.error.playerOffline")
            return
        }
        val targetComponent = store.ensureAndGetComponent(target.reference!!, PrivateMessageComponent.COMPONENT_TYPE)
        targetComponent.lastMessage = PlayerId(player.uuid)
        target.sendMessage("message.pm.targetMessage", "username" to player.username, "message" to message)
        player.sendMessage("message.pm.senderMessage", "username" to player.username, "message" to message)
    }

    override fun canGeneratePermission(): Boolean = false
}