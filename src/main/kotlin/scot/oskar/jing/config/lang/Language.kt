package scot.oskar.jing.config.lang

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
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.walk

//TODO: Clean this up so it's less ugly
class I18nService(basePath: Path) {

    private var langMap = mutableMapOf<Locale, I18nLangFile>()

    val logger = HytaleLogger.forEnclosingClass()
    val langDir: Path = basePath.resolve("lang")

    // TODO: Not sure if I like this but you cant exactly traverse resources in a jar
    val bundledLocales = listOf(
        "en-US"
    )

    fun ensureDefaults() {
        Files.createDirectories(langDir)
        for (locale in bundledLocales) {
            val target = langDir.resolve("$locale.json")
            if (Files.notExists(target)) {
                javaClass.getResourceAsStream("/lang/$locale.json")?.use { stream ->
                    Files.copy(stream, target)
                    logger.atInfo().log("Initialising default translation file: $locale.json")
                } ?: logger.atWarning().log("Bundled lang resource missing: /lang/$locale.json")
            }
        }
    }

    fun loadAll() {
        langDir.walk()
            .filter { it.isRegularFile() }
            .filter { it.name.endsWith(".json") }
            .filter { Locale.forLanguageTag(it.nameWithoutExtension).language.isNotEmpty() }
            .forEach { file ->
                logger.atInfo().log("Loading file: ${file.name}")

                BsonUtil.readDocument(file).thenAccept {
                    val decoded = I18nLangFile.CODEC.decode(it, ExtraInfo.THREAD_LOCAL.get())?: run {
                        return@thenAccept logger.atSevere().log("Failed to decode file: ${file.name}")
                    }
                    registerFile(Locale.forLanguageTag(file.nameWithoutExtension), decoded)
                }
            }
    }

    private fun registerFile(locale: Locale, file: I18nLangFile) {
        logger.atInfo().log("Registered translation file for locale: $locale")
        langMap[locale] = file
    }

    fun sendMessage(player: PlayerRef, messageKey: String, vararg placeholders: Pair<String, String>) {
        val langFile = getLanguageForLocale(player.language) ?: getLanguageForLocale("en-US")!!
        val message = langFile.messageMap[messageKey] ?: Message.raw("Invalid message: $messageKey")
        player.sendMessage(message.replace(*placeholders))
    }

    fun getLanguageForLocale(locale: String): I18nLangFile? {
        val locale = Locale.forLanguageTag(locale)
        return langMap[locale]
    }

}

object I18n {
    private lateinit var service: I18nService
    fun init(service: I18nService) { this.service = service }

    fun sendMessage(player: PlayerRef, key: String, vararg placeholders: Pair<String, String>) =
        service.sendMessage(player, key, *placeholders)
}

class I18nLangFile {

    companion object {
        val CODEC = BuilderCodec.builder(I18nLangFile::class.java) { I18nLangFile() }
            .append(
                KeyedCodec("Messages", MapCodec(Message.CODEC) { mutableMapOf<String, Message>() }),
                { o, v -> o.messageMap = v},
                { o -> o.messageMap})
            .add()
            .build()
    }

    var messageMap = mutableMapOf<String, Message>()

}