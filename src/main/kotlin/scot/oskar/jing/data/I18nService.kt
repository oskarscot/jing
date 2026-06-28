package scot.oskar.jing.data

import com.hypixel.hytale.codec.ExtraInfo
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.PluginBase
import com.hypixel.hytale.server.core.util.BsonUtil
import org.bson.BsonValue
import scot.oskar.jing.config.lang.I18nLangFile
import scot.oskar.jing.data.repository.CodecRepository
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.walk

class I18nService private constructor (basePath: Path): CodecRepository<I18nLangFile> {

    companion object Factory: HytaleAbstractServiceFactory<I18nService>() {
        override fun create(plugin: PluginBase): I18nService = I18nService(plugin.dataDirectory)
    }

    private val defaultTag = "en-US"
    private val defaultLocale: Locale = Locale.forLanguageTag(defaultTag)
    private val bundledLocales = listOf(defaultTag)
    private val logger = HytaleLogger.forEnclosingClass()
    private val langDir: Path = basePath.resolve("lang")
    private val langMap = ConcurrentHashMap<Locale, I18nLangFile>()

    private fun ensureDefaults() {
        Files.createDirectories(langDir)
        for (tag in bundledLocales) {
            val target = langDir.resolve("$tag.json")
            if (Files.exists(target)) continue

            val resource = javaClass.getResourceAsStream("/lang/$tag.json")
            if (resource == null) {
                logger.atWarning().log("Bundled lang resource missing: /lang/$tag.json")
                continue
            }
            resource.use { Files.copy(it, target) }
            logger.atInfo().log("Initialising default translation file: $tag.json")
        }
    }

    override fun loadAll(): CompletableFuture<List<I18nLangFile>> {
        ensureDefaults()
        val futures = langDir.walk()
            .filter { it.isRegularFile() && it.name.endsWith(".json") }
            .mapNotNull { file ->
                val locale = Locale.forLanguageTag(file.nameWithoutExtension)
                if (locale.language.isEmpty()) {
                    logger.atWarning().log("Skipping file with unparseable locale: ${file.name}")
                    return@mapNotNull null
                }
                logger.atInfo().log("Loading lang file: ${file.name}")
                BsonUtil.readDocument(file).thenApply { doc ->
                    val decoded = decode(doc)
                    if (decoded == null) {
                        logger.atSevere().log("Failed to decode file: ${file.name}")
                        null
                    } else {
                        langMap[locale] = decoded
                        logger.atInfo().log("Registered translation file for locale: $locale")
                        decoded
                    }
                }
            }
            .toList()
        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { futures.mapNotNull { it.join() } }
    }

    fun resolveOrDefault(languageTag: String): I18nLangFile =
        langMap[Locale.forLanguageTag(languageTag)] ?: langMap[defaultLocale]!!

    override fun decode(value: BsonValue): I18nLangFile? =
        I18nLangFile.CODEC.decode(value, ExtraInfo.THREAD_LOCAL.get())

}