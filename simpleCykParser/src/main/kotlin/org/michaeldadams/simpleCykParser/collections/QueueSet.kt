/** Wrappers for maps that implement default values. */

package org.michaeldadams.simpleCykParser.collections

class QueueSet<E>() : HashSet<E>() {
  private val elements: ArrayList<E> = ArrayList()

  // val size: Int
  // fun contains(element: E): Boolean
  // fun getElement(element: E): E?
  // fun isEmpty(): Boolean

  override fun add(element: E): Boolean {
    // TODO: some way to do this with 'also'?
    val result = super.add(element)
    if (result) elements.add(element)
    return result
  }
  override fun clear() = throw UnsupportedOperationException()
  override fun iterator(): MutableIterator<E> = object : MutableIterator<E> {
    var elementIndex: Int = 0
    override fun hasNext(): Boolean = elementIndex < elements.size
    // TODO: increment elementIndex
    override fun next(): E = if (hasNext()) elements[elementIndex] else throw NoSuchElementException()
    override fun remove(): Unit = throw UnsupportedOperationException()
  }
  override fun remove(element: E): Boolean = throw UnsupportedOperationException()
  // fun addAll(elements: Collection<E>): Boolean
  // fun removeAll(elements: Collection<E>): Boolean
  // fun retainAll(elements: Collection<E>): Boolean
  // open fun addAll(elements: Collection<E>): Boolean
  // open fun containsAll(elements: Collection<E>): Boolean
  // open fun removeAll(elements: Collection<E>): Boolean
  // open fun retainAll(elements: Collection<E>): Boolean
}
