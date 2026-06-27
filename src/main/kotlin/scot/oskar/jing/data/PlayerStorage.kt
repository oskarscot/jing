package scot.oskar.jing.data

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.validation.Validators
import java.util.UUID

data class JingPlayerData (
    var test: String = "",
) {
    companion object {
        val CODEC = BuilderCodec.builder(JingPlayerData::class.java) { JingPlayerData() }
            .append(
                KeyedCodec("Test", Codec.STRING),
                { o, v -> o.test = v },
                { o -> o.test}
            )
            .addValidator(Validators.nonEmptyString())
            .add()
            .build()
    }
}

@JvmInline
value class PlayerId(val id: UUID)