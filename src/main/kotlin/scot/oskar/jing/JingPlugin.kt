package scot.oskar.jing

import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.util.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import scot.oskar.jing.config.JingPluginConfiguration
import scot.oskar.jing.config.JingPluginConfigurationCodec
import scot.oskar.jing.config.lang.I18n
import scot.oskar.jing.data.I18nService
import scot.oskar.jing.data.storage.JingPlayerDataProvider.Companion.registerDataProvider
import scot.oskar.jing.data.storage.SimplePlayerDataProvider
import scot.oskar.jing.module.ModuleService

class JingPlugin(init: JavaPluginInit): JavaPlugin(init) {

    companion object {
        val SCOPE = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    val pluginConfig: Config<JingPluginConfiguration>
    val moduleService = ModuleService.create(this)
    private val i18nService = I18nService.create(this)

    init {
        SimplePlayerDataProvider.basePath = dataDirectory

        registerDataProvider(provider = SimplePlayerDataProvider()) { SimplePlayerDataProvider.CODEC }

        pluginConfig = withConfig(JingPluginConfigurationCodec)
    }

    override fun setup() {
        logger.atInfo().log("Using ${pluginConfig.get().storageProvider::class.simpleName} as storage provider.")

        i18nService.loadAll()
        I18n.init(i18nService)

        moduleService.loadModules()

        pluginConfig.save()
    }

    override fun shutdown() {
        SCOPE.cancel()
    }
}