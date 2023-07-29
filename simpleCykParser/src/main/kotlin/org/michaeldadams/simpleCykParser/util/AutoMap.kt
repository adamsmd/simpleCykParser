/** The [TotalMap] and [AutoMap] classes. */

package org.michaeldadams.simpleCykParser.util

/**
 * A [Map] whose [get] function always returns a value (i.e., [get] returns [V]
 * instead of [V?]) and thus represents a total map instead of the partial map
 * represented by a [Map].
 *
 * The main use of this is a read-only wrapper around [AutoMap], so the only way
 * to add new entries is with autovivification via [get].
 *
 * @param K the type of map keys
 * @param V the type of map values
 */
interface TotalMap<K, out V> : Map<K, V> {
  // Same as [get] in [Map] except that this returns [V] instead of [V?]
  override operator fun get(key: K): V
}

/**
 * A [Map] that upon lookup automatically populates missing entries with default
 * values (i.e., autovivification).
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @property defaultValue the function for generating default values for a given
 *   key
 */
class AutoMap<K, V>(private val defaultValue: (K) -> V) : HashMap<K, V>(), TotalMap<K, V> {
  // Getting adds if not already present
  override operator fun get(key: K): V =
    // We use [contains] instead of [getOrPut] since the latter is incorrectly
    // implemented if [V] is a nullable type.
    if (this.contains(key)) {
      @Suppress("UNCHECKED_CAST")
      super.get(key) as V
    } else {
      defaultValue(key).also { this.put(key, it) }
    }
}
