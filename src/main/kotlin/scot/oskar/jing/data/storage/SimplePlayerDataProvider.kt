package scot.oskar.jing.data.storage

import com.hypixel.hytale.codec.ExtraInfo
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.server.core.util.BsonUtil
import kotlinx.coroutines.future.await
import scot.oskar.jing.data.JingPlayerData
import scot.oskar.jing.data.PlayerId
import java.nio.file.Files
import java.nio.file.Path

class SimplePlayerDataProvider : JingPlayerDataProvider {

    companion object {
        lateinit var basePath: Path
        val CODEC = BuilderCodec.builder(SimplePlayerDataProvider::class.java) { SimplePlayerDataProvider() }.build()
    }

    private var dataPath = basePath.resolve("storage/players")
    private val fileExtension = "json"

    override suspend fun getOrCreate(id: PlayerId, playerData: () -> JingPlayerData): JingPlayerData =
        load(id) ?: save(id, playerData())

    override suspend fun load(id: PlayerId): JingPlayerData? {
        val path = dataPath.resolve("${id.id}.$fileExtension")

        if(!Files.isRegularFile(path)) return null

        val document = BsonUtil.readDocument(path).await()
        val playerData = JingPlayerData.CODEC.decode(document, ExtraInfo.THREAD_LOCAL.get()) ?: return null

        return playerData
    }

    override suspend fun save(id: PlayerId, data: JingPlayerData): JingPlayerData {
        val path = dataPath.resolve("${id.id}.$fileExtension")

        val encodedDocument = JingPlayerData.CODEC.encode(data, ExtraInfo.THREAD_LOCAL.get())
        BsonUtil.writeDocument(path, encodedDocument, true).await()

        return data
    }
}