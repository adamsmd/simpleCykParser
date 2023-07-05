/** Wrappers for maps that implement default values. */

package org.michaeldadams.simpleCykParser.collections

import kotlin.collections.MutableMap

/**
 * Wrapper for maps that upon lookup automatically populates missing entries
 * with default values (i.e., autovivification).
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @param M the type of the map to be wrapped
 * @property map the map to be wrapped
 * @property defaultValue a function generating default values
 */
class AutoMap<K, V>(private val map: MutableMap<K, V>, private val defaultValue: () -> V) :
  MutableMap<K, V> by map {
  override operator fun get(key: K): V = map.getOrPut(key, defaultValue)

  /**
   * Create a [AutoMap] with a new [MutableMap].
   *
   * @param K the type of map keys
   * @param V the type of map values
   * @param defaultValue a function generating default values
   * @return a [AutoMap] using [defaultValue]
   */
  constructor(defaultValue: () -> V) : this(mutableMapOf(), defaultValue)

}
