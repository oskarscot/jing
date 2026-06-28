package scot.oskar.jing.data

abstract class AbstractServiceFactory<T> {

    abstract fun create(): T
}