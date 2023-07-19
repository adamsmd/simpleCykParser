/** Code and helpers for [AutoMap] and [MutableAutoMap]. */

package org.michaeldadams.simpleCykParser.util

/**
 * TODO.
 *
 * This interface is a read-only wrapper around the mutable implementation, so
 * the only way to add new entries is with autovivification via [get].
 *
 * @param K the type of map keys
 * @param V the type of map values
 */
interface TotalMap<K, out V> : Map<K, V> {
  /**
   * TODO: never null (unless V is nullable).
   *
   * @param key
   * @return TODO
   */
  override operator fun get(key: K): V
}

// ================================================================== //
// Private Implementation
// ================================================================== //

/**
 * A [Map] that upon lookup automatically populates missing entries with default
 * values (i.e., autovivification).
 *
 * TODO
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @property defaultValue a function generating default values
 */
class AutoMap<K, V>(private val defaultValue: (K) -> V) : HashMap<K, V>(), TotalMap<K, V> {
  // Getting adds if not already present
  override operator fun get(key: K): V =
    if (this.contains(key)) {
      @Suppress("UNCHECKED_CAST")
      super.get(key) as V
    } else {
      defaultValue(key).also { this.put(key, it) }
    }
}
