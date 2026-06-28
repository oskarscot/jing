package scot.oskar.jing.data

import com.hypixel.hytale.server.core.plugin.PluginBase

abstract class HytaleAbstractServiceFactory<T> {

    abstract fun create(plugin: PluginBase): T

}