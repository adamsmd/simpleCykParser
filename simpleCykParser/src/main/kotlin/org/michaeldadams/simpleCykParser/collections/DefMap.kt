/* TODO: document package */
/* TODO: put in package */
/* TODO: put in "collections.defmap" package */

import java.util.LinkedHashMap
import java.util.TreeMap

/**
 * Wrapper for maps that implements default values. Looking up a non-existant
 * value adds the value returned by `defaultValue` to the map.
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @param M the type of the map to be wrapped
 * @param map the map to be wrapped
 * @param devalueValue a function generating default values
 */
class DefMap<K, V, M : MutableMap<K, V>>(val map: M, val defaultValue: () -> V) :
  MutableMap<K, V> by map {
  override fun get(key: K): V = getOrPut(key, defaultValue)
}

/**
 * `DefMap` specialized to wrap a `LinkedHashMap`.
 *
 * @param K the type of map keys
 * @param V the type of map values
 */
typealias DefHashMap<K, V> = DefMap<K, V, LinkedHashMap<K, V>>

/**
 * Create a `DefHashMap` with a new `LinkedHashMap`.
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @param defaultValue a function generating default values
 */
fun <K, V> defHashMap(defaultValue: () -> V): DefHashMap<K, V> = DefMap(LinkedHashMap(), defaultValue)

/**
 * `DefMap` specialized to wrap a `TreeMap`.
 *
 * @param K the type of map keys
 * @param V the type of map values
 */
typealias DefTreeMap<K, V> = DefMap<K, V, TreeMap<K, V>>

/**
 * Create a `DefTreeMap` with a new `TreeMap`.
 *
 * @param K the type of map keys
 * @param V the type of map values
 * @param defaultValue a function generating default values
 */
fun <K, V> defTreeMap(defaultValue: () -> V): DefTreeMap<K, V> = DefMap(TreeMap(), defaultValue)
