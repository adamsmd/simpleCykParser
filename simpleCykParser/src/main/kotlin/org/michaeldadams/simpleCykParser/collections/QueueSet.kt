/** Wrappers for maps that implement default values. */

package org.michaeldadams.simpleCykParser.collections

class QueueSet<E> : HashSet<E>() {
  private val elements: ArrayList<E> = ArrayList()

  override fun add(element: E): Boolean = super.add(element).also { if (it) elements.add(element) }
  override fun clear(): Unit = throw UnsupportedOperationException()
  override fun remove(element: E): Boolean = throw UnsupportedOperationException()
  override fun iterator(): MutableIterator<E> = object : MutableIterator<E> {
    private var indexOfNext: Int = 0
    override fun hasNext(): Boolean = indexOfNext < elements.size
    override fun next(): E =
      if (this.hasNext()) elements[indexOfNext++] else throw NoSuchElementException()
    override fun remove(): Unit = throw UnsupportedOperationException()
  }
}
