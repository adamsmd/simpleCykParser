/** Code and helpers for [QueueMap]. */

package org.michaeldadams.simpleCykParser.util

// TODO: Rename to AutoMap and MutableAutoMap

/**
 * A [Map] that upon lookup automatically populates missing entries with default
 * values (i.e., autovivification).
 *
 * This interface is a read-only wrapper around the mutable implementation, so
 * the only way to add new entries is with autovivification via [get].
 *
 * @param K the type of map keys
 * @param V the type of map values
 */
interface QueueMap<K, out V> : Map<K, V> {
  /**
   * TODO: never null (unless V is nullable).
   *
   * @param key
   * @return TODO
   */
  override operator fun get(key: K): V
}

/**
 * TODO.
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @param defaultValue a function generating default values
 * @return TODO
 */
fun <K, V> queueMap(defaultValue: (K) -> V): QueueMap<K, V> = QueueMapImpl(defaultValue)

// ================================================================== //
// Private Implementation
// ================================================================== //

/**
 * TODO.
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @property defaultValue a function generating default values
 */
private class QueueMapImpl<K, V>(private val defaultValue: (K) -> V) : HashMap<K, V>(), QueueMap<K, V> {
  // Getting adds if not already present
  override operator fun get(key: K): V =
    if (this.contains(key)) {
      @Suppress("UNCHECKED_CAST")
      super.get(key) as V
    } else {
      defaultValue(key).also { this.put(key, it) }
    }
}
