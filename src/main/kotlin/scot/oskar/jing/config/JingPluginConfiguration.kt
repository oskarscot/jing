package scot.oskar.jing.config

import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec
import com.hypixel.hytale.codec.validation.Validators
import scot.oskar.jing.data.storage.JingPlayerDataProvider
import scot.oskar.jing.data.storage.SimplePlayerDataProvider
import scot.oskar.jing.module.AbstractJingModule

class JingPluginConfiguration {

    companion object {
        val CODEC = BuilderCodec.builder(JingPluginConfiguration::class.java) { JingPluginConfiguration() }
            .append(
                KeyedCodec("StorageProvider", JingPlayerDataProvider.CODEC),
                { o, v -> o.storageProvider = v },
                { o -> o.storageProvider }
            )
            .addValidator(Validators.nonNull())
            .add()
            .append(
                KeyedCodec("EnabledModules", ArrayCodec(AbstractJingModule.CODEC) { arrayOf() }),
                { o, v -> o.enabledModules = v},
                { o -> o.enabledModules }
            )
            .addValidator(Validators.nonEmptyArray())
            .addValidator(Validators.nonNullArrayElements())
            .add()
            .build()
    }

    var storageProvider: JingPlayerDataProvider = SimplePlayerDataProvider()
    var enabledModules: Array<AbstractJingModule> = emptyArray()

}