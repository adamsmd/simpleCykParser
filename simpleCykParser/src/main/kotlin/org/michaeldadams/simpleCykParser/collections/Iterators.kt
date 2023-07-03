package org.michaeldadams.simpleCykParser.collections.iterators

/* TODO: document package */
/* TODO: put in package */
/* TODO: put in "collections.defmap" package */

import java.util.NavigableMap
import java.util.Queue

/**
 * Iterator over the elements of a `NavigableMap`
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @param map the map to be iterated over
 */
open class NavigableIterator<K, V>(val map: NavigableMap<K, V>) : Iterator<Map.Entry<K, V>> {
  /** The key currently pointed to by the iterator.  Contains `null` before first call to `next`. */
  protected var key: K? = null

  /** Computes the next entry in the map. */
  protected open fun nextEntry(): Map.Entry<K, V>? = if (key == null) map.firstEntry() else map.higherEntry(key)

  override fun hasNext(): Boolean = nextEntry() != null

  override fun next(): Map.Entry<K, V> =
    nextEntry()?.let { key = it.key; it } ?: throw NoSuchElementException()
}

/**
 * Iterator (in reverse order) over the elements of a `NavigableMap`
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @param map the map to be iterated over
 */
class ReverseNavigableIterator<K, V>(map: NavigableMap<K, V>) : NavigableIterator<K, V>(map) {
  protected override fun nextEntry(): Map.Entry<K, V>? = if (key == null) map.lastEntry() else map.lowerEntry(key)
}

/**
 * Iterator over the elements of a `Queue` until the `Queue` is empty.
 *
 * @param T the type of queue elements
 * @param queue the queue to be iterated over
 */
class QueueIterator<T>(val queue: Queue<T>) : Iterator<T> {
  override fun hasNext(): Boolean = !queue.isEmpty()
  override fun next(): T = queue.remove()
}
