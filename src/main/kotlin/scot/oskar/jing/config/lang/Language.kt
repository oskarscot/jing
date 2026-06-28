package scot.oskar.jing.config.lang

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.ExtraInfo
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.util.BsonUtil
import scot.oskar.jing.ext.replace
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.walk

class I18nService (basePath: Path) {

    private companion object {
        const val DEFAULT_TAG = "en-US"
        val DEFAULT_LOCALE: Locale = Locale.forLanguageTag(DEFAULT_TAG)
        val BUNDLED_LOCALES = listOf(DEFAULT_TAG)
    }

    private val logger = HytaleLogger.forEnclosingClass()
    private val langDir: Path = basePath.resolve("lang")
    private val langMap = ConcurrentHashMap<Locale, I18nLangFile>()

    fun load(): CompletableFuture<Void> {
        ensureDefaults()
        return loadAll().thenRun {
            if (!langMap.containsKey(Locale.forLanguageTag(DEFAULT_TAG))) {
                logger.atSevere()
                    .log("Default locale $DEFAULT_TAG failed to load; messages will fall back to raw keys")
            }
        }
    }

    private fun ensureDefaults() {
        Files.createDirectories(langDir)
        for (tag in BUNDLED_LOCALES) {
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

    private fun loadAll(): CompletableFuture<Void> {
        val futures = langDir.walk()
            .filter { it.isRegularFile() && it.name.endsWith(".json") }
            .mapNotNull { file ->
                val locale = Locale.forLanguageTag(file.nameWithoutExtension)
                if (locale.language.isEmpty()) {
                    logger.atWarning().log("Skipping file with unparseable locale: ${file.name}")
                    return@mapNotNull null
                }
                logger.atInfo().log("Loading lang file: ${file.name}")
                BsonUtil.readDocument(file).thenAccept { doc ->
                    val decoded = I18nLangFile.CODEC.decode(doc, ExtraInfo.THREAD_LOCAL.get())
                    if (decoded == null) {
                        logger.atSevere().log("Failed to decode file: ${file.name}")
                    } else {
                        langMap[locale] = decoded
                        logger.atInfo().log("Registered translation file for locale: $locale")
                    }
                }
            }
            .toList()
        return CompletableFuture.allOf(*futures.toTypedArray())
    }

    fun resolve(languageTag: String): I18nLangFile? =
        langMap[Locale.forLanguageTag(languageTag)] ?: langMap[DEFAULT_LOCALE]
}

object I18n {

    private lateinit var service: I18nService

    fun init(service: I18nService) { this.service = service }

    fun sendMessage(player: PlayerRef, key: String, vararg placeholders: Pair<String, String>) {
        val message = service.resolve(player.language)?.messageMap?.get(key)
            ?: Message.raw("Invalid message: $key")
        player.sendMessage(message.replace(*placeholders))
    }

    fun getValue(player: PlayerRef, key: String): String =
        service.resolve(player.language)?.valueMap?.get(key)
            ?: "Missing value: $key"
}

class I18nLangFile {
    var messageMap = mutableMapOf<String, Message>()
        private set
    var valueMap = mutableMapOf<String, String>()
        private set

    companion object {
        val CODEC = BuilderCodec.builder(I18nLangFile::class.java) { I18nLangFile() }
            .append(
                KeyedCodec("Messages", MapCodec(Message.CODEC) { mutableMapOf<String, Message>() }),
                { o, v -> o.messageMap = v },
                { o -> o.messageMap },
            ).add()
            .append(
                KeyedCodec("Values", MapCodec(Codec.STRING) { mutableMapOf<String, String>() }, false),
                { o, v -> o.valueMap = v },
                { o -> o.valueMap },
            ).add()
            .build()
    }
}