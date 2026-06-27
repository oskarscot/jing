package scot.oskar.jing

import gg.ginco.jellyparty.codec.annotations.SerialWithCodec
import gg.ginco.jellyparty.codec.annotations.SerializableObject
import scot.oskar.jing.data.storage.JingPlayerDataProvider
import scot.oskar.jing.data.storage.SimplePlayerDataProvider

@SerializableObject
class JingConfiguration {

    @SerialWithCodec("scot.oskar.jing.data.storage.JingPlayerDataProvider.CODEC")
    var storageProvider: JingPlayerDataProvider = SimplePlayerDataProvider()

}