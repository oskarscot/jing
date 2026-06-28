package scot.oskar.jing.data

import scot.oskar.jing.JingPlugin

abstract class AbstractHytaleServiceFactory<T> {

    abstract fun create(plugin: JingPlugin): T

}