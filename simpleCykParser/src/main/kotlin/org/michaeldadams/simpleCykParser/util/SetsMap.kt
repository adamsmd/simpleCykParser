/** TODO. */

package org.michaeldadams.simpleCykParser.util

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun <K, V> Iterable<Pair<K, V>>.toSetsMap(): Map<K, Set<V>> =
  this.groupBy { it.first }.mapValues { entry -> entry.value.map { it.second }.toSet() }

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun <K, V> Map<K, Set<V>>.fromSetsMap(): List<Pair<K, V>> =
  this.flatMap { (lhs, rhsSet) -> rhsSet.map { lhs to it } }
