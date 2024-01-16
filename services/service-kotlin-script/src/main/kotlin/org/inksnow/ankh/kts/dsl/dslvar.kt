package org.inksnow.ankh.kts.dsl

import net.kyori.adventure.builder.AbstractBuilder
import net.kyori.adventure.util.Buildable
import java.util.concurrent.atomic.AtomicReference

class DslVar<T>(
    private val getAction: () -> T,
    private val setAction: (T) -> Unit
) {
    fun get(): T {
        return getAction()
    }

    fun set(value: T) {
        return setAction(value)
    }

    operator fun invoke(): T {
        return getAction()
    }

    operator fun invoke(value: T) {
        setAction(value)
    }
}

class DslVarCollection<T>(
    private val value: List<DslVar<T>>
) {
    val `accessor$value` get() = value

    operator fun get(index: Int): DslVar<T> {
        return value[index]
    }

    operator fun set(index: Int, value: T) {
        this[index].set(value)
    }

    operator fun invoke(): List<T> {
        return value.map { it.get() }
    }

    operator fun invoke(value: T) {
        this.value.forEach { it(value) }
    }

    fun each(action: () -> T) {
        value.forEach {
            it(action())
        }
    }
}

@JvmName("invoke\$buildable")
inline operator fun <T : Buildable<T, B>, B : AbstractBuilder<T>> DslVar<T>.invoke(action: B.() -> Unit) {
    set(get().toBuilder().apply(action).build())
}

@JvmName("invoke\$buildable")
inline operator fun <T : Buildable<T, B>, B : AbstractBuilder<T>> DslVarCollection<T>.invoke(action: B.() -> Unit) {
    `accessor$value`.forEach { it(action) }
}

@JvmName("invoke\$dsl")
inline operator fun <T : IDsl> DslVar<T>.invoke(action: T.() -> Unit) {
    get().action()
}

@JvmName("invoke\$dsl")
inline operator fun <T : IDsl> DslVarCollection<T>.invoke(action: T.() -> Unit) {
    `accessor$value`.forEach { it(action) }
}

fun <T> createVar(): DslVar<T> {
    val ref = AtomicReference<T>()
    return DslVar(ref::get, ref::set)
}

fun <T> createVar(get: () -> T, set: (T) -> Unit): DslVar<T> {
    return DslVar(get, set)
}


fun <E, T> createVarCollection(elementList: List<E>, get: (E) -> T, set: (E, T) -> Unit): DslVarCollection<T> {
    return DslVarCollection(elementList.map { element ->
        createVar({ get(element) }, { set(element, it) })
    })
}