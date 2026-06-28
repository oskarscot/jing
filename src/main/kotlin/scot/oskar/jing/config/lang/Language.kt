package scot.oskar.jing.config.lang

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.universe.PlayerRef
import scot.oskar.jing.data.I18nService
import scot.oskar.jing.ext.replace

object I18n {

    private lateinit var service: I18nService

    fun init(service: I18nService) { this.service = service }

    fun sendMessage(player: PlayerRef, key: String, vararg placeholders: Pair<String, String>) {
        val message = service.resolveOrDefault(player.language).messageMap[key]
            ?: Message.raw("Invalid message: $key")
        player.sendMessage(message.replace(*placeholders))
    }

    fun getValue(player: PlayerRef, key: String): String =
        service.resolveOrDefault(player.language).valueMap[key]
            ?: "Missing value: $key"
}

class I18nLangFile {
    var messageMap = mutableMapOf<String, Message>()
        private set
    var valueMap = mutableMapOf<String, String>()
        private set

    companion object {
        @JvmField
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