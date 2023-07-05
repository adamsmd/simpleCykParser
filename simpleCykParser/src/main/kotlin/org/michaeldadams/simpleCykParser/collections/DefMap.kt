/** Wrappers for maps that implement default values. */

package org.michaeldadams.simpleCykParser.collections

import kotlin.collections.MutableMap

/**
 * Wrapper for maps that implements default values. Looking up a non-existant
 * value adds the value returned by [defaultValue] to the map.
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @param M the type of the map to be wrapped
 * @property map the map to be wrapped
 * @property defaultValue a function generating default values
 */
class DefMap<K, V>(val map: MutableMap<K, V>, val defaultValue: () -> V) :
  MutableMap<K, V> by map {
  override operator fun get(key: K): V = map.getOrPut(key, defaultValue)
}

/**
 * Create a [DefMap] with a new [MutableMap].
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @param defaultValue a function generating default values
 * @return a [DefMap] using [defaultValue]
 */
fun <K, V> defMap(defaultValue: () -> V): DefMap<K, V> = DefMap(mutableMapOf(), defaultValue)
