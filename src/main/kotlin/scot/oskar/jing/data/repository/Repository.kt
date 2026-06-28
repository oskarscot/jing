package scot.oskar.jing.data.repository

import java.util.concurrent.CompletableFuture

interface Repository<T> {

    /**
     *  Loads all [T] entities
     */
    fun loadAll(): CompletableFuture<List<T>>

}