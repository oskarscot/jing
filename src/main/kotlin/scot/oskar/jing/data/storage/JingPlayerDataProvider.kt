package scot.oskar.jing.data.storage

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec
import com.hypixel.hytale.codec.lookup.Priority
import com.hypixel.hytale.codec.lookup.Priority.DEFAULT
import scot.oskar.jing.data.JingPlayerData
import scot.oskar.jing.data.PlayerId

interface JingPlayerDataProvider {

    companion object {
        @JvmField
        val CODEC = BuilderCodecMapCodec<JingPlayerDataProvider>("Type", true)

        /**
         * Registers a new [JingPlayerDataProvider] with the [com.hypixel.hytale.codec.lookup.MapKeyMapCodec] for
         * data providers
         *
         * @param priority the priority to use for registration or [Priority.DEFAULT] if not provided
         * @param provider the actual [JingPlayerDataProvider]
         * @param codec the Hytale [com.hypixel.hytale.codec.Codec] supplier
         */
        fun <T: JingPlayerDataProvider> registerDataProvider(
            priority: Priority = DEFAULT,
            provider: T,
            codec: () -> BuilderCodec<T>
        ) {
            CODEC.register(priority, provider::class.simpleName!!, provider::class.java, codec())
        }
    }

    /**
     * Retrieves and returns the [JingPlayerData] for the specified [PlayerId] or creates it
     * by calling [save] and invokes the Unit
     *
     * @param id typed wrapper for the [java.util.UUID] of the [com.hypixel.hytale.server.core.universe.PlayerRef]
     * @param playerData Unit to invoke when creating the [JingPlayerData]
     */
    suspend fun getOrCreate(id: PlayerId, playerData: () -> JingPlayerData): JingPlayerData

    /**
     * Loads [JingPlayerData] for the specified [PlayerId]
     *
     * @param id typed wrapper for the [java.util.UUID] of the [com.hypixel.hytale.server.core.universe.PlayerRef]
     *
     * @return loaded [JingPlayerData] or `null`
     */
    suspend fun load(id: PlayerId): JingPlayerData?

    /**
     * Saves the specified [data] for the [PlayerId]
     *
     * @param id typed wrapper for the [java.util.UUID] of the [com.hypixel.hytale.server.core.universe.PlayerRef]
     * @param data the [JingPlayerData] instance to save
     *
     * @return the saved [JingPlayerData]
     */
    suspend fun save(id: PlayerId, data: JingPlayerData): JingPlayerData
}