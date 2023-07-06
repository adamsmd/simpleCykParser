/** Wrappers for maps that implement default values. */

package org.michaeldadams.simpleCykParser.collections

import kotlin.collections.MutableMap

interface QueueMap<K, out V> : Map<K, V> {
  val keysQueue: Set<K>
  override operator fun get(key: K): V
}

fun <K, V> QueueMap(defaultValue: () -> V): QueueMap<K, V> = QueueMapImpl(defaultValue)

/**
 * [MutableMap] that upon lookup automatically populates missing entries with
 * default values (i.e., autovivification).
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @property defaultValue a function generating default values
 */
private class QueueMapImpl<K, V>(val defaultValue: () -> V) : HashMap<K, V>(), QueueMap<K, V> {
  /**
   * Create an [AutoMap] with a new, empty [MutableMap].
   *
   * @param defaultValue a function generating default values
   */
  // constructor(defaultValue: () -> V) : this(mutableMapOf(), defaultValue)
  override val keysQueue: MutableSet<K> = QueueSet()
  override fun put(key: K, value: V): V? = super.put(key, value).also { keysQueue += key }
  // TODO: remove is UnsupportedException

  // TODO: does delegation cover everything or do I need inheritance?
  override operator fun get(key: K): V =
    if (this.contains(key)) super.get(key) as V
    else defaultValue().also { this.put(key, it) }
}
