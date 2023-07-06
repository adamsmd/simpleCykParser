/** Code and helpers for [QueueMap]. */

package org.michaeldadams.simpleCykParser.collections

/**
 * [Map] that upon lookup automatically populates missing entries with default
 * values (i.e., autovivification) and maintains a [QueueSet] of the keys.
 *
 * This interface provides a read-only wrapper around the mutable implementation.
 * So the only way to add new entries is with [get].
 *
 * @param K the type of map keys
 * @param V the type of map values
 */
interface QueueMap<K, out V> : Map<K, V> {
  /** TODO: backed by a QueueSet, but is Set so that it is not user modifiable. */
  val keysQueue: Set<K>

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
 * @property defaultValue a function generating default values
 */
private class QueueMapImpl<K, V>(val defaultValue: () -> V) : HashMap<K, V>(), QueueMap<K, V> {
  override val keysQueue: QueueSet<K> = QueueSet()
  
  // Putting also adds to [keysQueue]
  override fun put(key: K, value: V): V? = super.put(key, value).also { keysQueue += key }
  
  // Getting adds if not already present
  override operator fun get(key: K): V =
    if (this.contains(key)) {
      super.get(key) as V
    } else {
      defaultValue().also { this.put(key, it) }
    }

  // Removal operations are unsupported because they break [elements] and thus iterators.
  override fun clear(): Unit = throw UnsupportedOperationException()
  override fun remove(key: K): V? = throw UnsupportedOperationException()
}

/**
 * TODO.
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @param defaultValue a function generating default values
 * @return TODO
 */
fun <K, V> queueMap(defaultValue: () -> V): QueueMap<K, V> = QueueMapImpl(defaultValue)
