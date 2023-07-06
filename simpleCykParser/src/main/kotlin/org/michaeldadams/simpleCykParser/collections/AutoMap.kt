/** Wrappers for maps that implement default values. */

package org.michaeldadams.simpleCykParser.collections

import kotlin.collections.MutableMap

/**
 * Wrapper for maps that upon lookup automatically populates missing entries
 * with default values (i.e., autovivification).
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @property map the map to be wrapped
 * @property defaultValue a function generating default values
 */
class AutoMap<K, V>(private val map: MutableMap<K, V>, private val defaultValue: () -> V) :
  MutableMap<K, V> by map {
  /**
   * Create an [AutoMap] with a new, empty [MutableMap].
   *
   * @param defaultValue a function generating default values
   */
  constructor(defaultValue: () -> V) : this(mutableMapOf(), defaultValue)

  // TODO: does delegation cover everything or do I need inheritance?
  override operator fun get(key: K): V = map.getOrPut(key, defaultValue)
}
