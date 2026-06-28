package scot.oskar.jing.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class HomeCommand: AbstractPlayerCommand("home", "Teleport to your home") {

    override fun execute(context: CommandContext, store: Store<EntityStore>, ref: Ref<EntityStore>, playerRef: PlayerRef, world: World
    ) {
        TODO("Not yet implemented")
    }
}