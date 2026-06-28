package scot.oskar.jing.data.repository

import org.bson.BsonValue

interface CodecRepository<T>: Repository<T> {

    /**
     *  Decodes the [T] entity from a [BsonValue] primarily used for decoding
     *  Hytale serialised entities
     *
     *  @return [T] the nullable decoded entity
     */
    fun decode(value: BsonValue): T?

}