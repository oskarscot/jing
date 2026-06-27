package scot.oskar.jing.component

import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import scot.oskar.jing.data.PlayerId

class PrivateMessageComponent(
    var lastMessage: PlayerId? = null
): Component<EntityStore> {

    companion object {
        lateinit var COMPONENT_TYPE: ComponentType<EntityStore, PrivateMessageComponent>
    }

    override fun clone(): Component<EntityStore> = PrivateMessageComponent(lastMessage)
}