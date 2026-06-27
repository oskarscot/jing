package scot.oskar.jing.ext

import com.hypixel.hytale.protocol.FormattedMessage
import com.hypixel.hytale.server.core.Message

/**
 *  Makes a deep clone of the [FormattedMessage], replaces rawText in itself and it's children with the
 *  populated message
 *
 *  Hytale does not allow you to replace text in an already constructed Message, this makes
 *  configurable messages with placholders difficult for no reason.
 *
 *  @param placeholders vararg list of pairs of placeholder and it's value
 */
fun Message.replace(vararg placeholders: Pair<String, String>): Message {
    val copy = formattedMessage.clone()
    copy.fillPlaceholders(placeholders.toMap())
    return Message(copy)
}

fun FormattedMessage.fillPlaceholders(values: Map<String, String>) {
    rawText?.let { text ->
        rawText = values.entries.fold(text) { acc, (k, v) -> acc.replace("{$k}", v) }
    }
    children?.forEach { it.fillPlaceholders(values) }
    messageParams?.values?.forEach { it.fillPlaceholders(values) }
}