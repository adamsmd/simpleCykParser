/** TODO. */

package org.michaeldadams.simpleCykParser.util

/**
 * A [Set] implementation that allows one to iterate through it while adding to
 * it like one could with a queue.
 *
 * @param E the type of set elements
 */
class QueueSet<E> : HashSet<E>() {
  /** Set elements in the order they were added.  Used by the iterator. */
  private val elements: ArrayList<E> = ArrayList()

  // Adding requires also adding to [elements]
  override fun add(element: E): Boolean =
    super.add(element).also { if (it) elements.add(element) }

  // Removal operations are unsupported because they break [elements] and thus iterators.
  override fun clear(): Unit = throw UnsupportedOperationException()
  override fun remove(element: E): Boolean = throw UnsupportedOperationException()

  // This iterator just looks counts through the [elements] array
  override fun iterator(): MutableIterator<E> = object : MutableIterator<E> {
    private var indexOfNext: Int = 0
    override fun hasNext(): Boolean = indexOfNext < elements.size
    override fun next(): E =
      if (this.hasNext()) elements[indexOfNext++] else throw NoSuchElementException()
    override fun remove(): Unit = throw UnsupportedOperationException()
  }
}
