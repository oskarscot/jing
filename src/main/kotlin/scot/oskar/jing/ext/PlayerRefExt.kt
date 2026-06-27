package scot.oskar.jing.ext

import com.hypixel.hytale.server.core.universe.PlayerRef
import scot.oskar.jing.config.lang.I18n

fun PlayerRef.sendMessage(key: String, vararg placeholders: Pair<String, String>) {
    I18n.sendMessage(this, key, *placeholders)
}