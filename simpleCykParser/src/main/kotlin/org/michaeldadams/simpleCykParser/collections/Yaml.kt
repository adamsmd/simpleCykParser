package org.michaeldadams.simpleCykParser.collections

import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlPath
import com.charleskorn.kaml.YamlScalar

typealias ToYaml<T> = (T, YamlPath) -> YamlNode

fun <T> scalarToYaml(): ToYaml<T> = { it, path -> YamlScalar(it.toString(), path) }

fun <E> setToYaml(elementToYaml: ToYaml<E>): ToYaml<Set<E>> = { it, path ->
  YamlList(
    // TODO: sorted
    it.mapIndexed { i, element -> elementToYaml(element, path.withListEntry(i, path.endLocation)) },
    path
  )
}

fun <K, V> mapToYaml(valueToYaml: ToYaml<V>): ToYaml<Map<K, V>> = { it, path ->
  YamlMap(
    it.map { (key, value) ->
      Pair(
        YamlScalar(key.toString(), path.withMapElementKey(key.toString(), path.endLocation)),
        valueToYaml(value, path.withMapElementValue(path.endLocation))
      )
    }.toMap(),
    path
  )
}
