package scot.oskar.jing.data

import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec
import gg.ginco.jellyparty.codec.annotations.SerializableObject
import org.joml.Vector3d
import java.util.*

data class JingPlayerData (
    var homes: Array<PlayerHome> = emptyArray(),
) {
    companion object {
        val CODEC = BuilderCodec.builder(JingPlayerData::class.java) { JingPlayerData() }
            .append(
                KeyedCodec("HomeData", ArrayCodec(PlayerHomeCodec) { arrayOf(PlayerHome()) }),
                { o, v -> o.homes = v },
                { o -> o.homes}
            )
            .add()
            .build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JingPlayerData

        return homes.contentEquals(other.homes)
    }

    override fun hashCode(): Int {
        return Objects.hash(homes)
    }
}

@JvmInline
value class PlayerId(val id: UUID)

@SerializableObject
data class PlayerHome(
    var location: Vector3d = Vector3d(),
    var name: String = ""
)