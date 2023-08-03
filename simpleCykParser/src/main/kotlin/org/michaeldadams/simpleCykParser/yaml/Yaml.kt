/** Functions for converting Yaml to a grammar. */

package org.michaeldadams.simpleCykParser.yaml

import com.charleskorn.kaml.IncorrectTypeException
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlScalar
import org.snakeyaml.engine.v2.api.Dump
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.Tag
import java.io.StringWriter

// ================================================================== //
// Parse YAML
// ================================================================== //

/**
 * Parses a string as Yaml document.
 *
 * @receiver the string containing the Yaml to parse
 * @return the [YamlNode] resulting from parsing the string
 */
fun String.toYaml(): YamlNode = Yaml.default.parseToYamlNode(this)

// ================================================================== //
// Ints
// ================================================================== //

// TODO: document
fun YamlNode.toIntOrNull(): Int? =
  when (this) {
    is YamlScalar -> this.toInt()
    is YamlNull -> null
    else -> throw incorrectType("YamlScalar or YamlNull", this)
  }

// ================================================================== //
// Strings
// ================================================================== //

/**
 * Convert a [String] object to its YAML representation.
 *
 * We do not use `Yaml.default.encodeToString(String.serializer(), s)` because
 * that produced `--- ""` when applied to the empty string.
 *
 * @receiver the object to convert to YAML
 * @return the YAML resulting from converting the object
 */
fun String.toYamlString(): String {
  val writer = object : StringWriter(), StreamDataWriter {
    override fun flush(): Unit = super<StringWriter>.flush()
  }
  // Other potentially useful settings are setWidth() and setSplitLines()
  val builder = DumpSettings.builder().setUseUnicodeEncoding(false).setBestLineBreak("").build()
  Dump(builder).dumpNode(ScalarNode(Tag.STR, this, ScalarStyle.DOUBLE_QUOTED), writer)
  return writer.toString()
}

// ================================================================== //
// Type Error
// ================================================================== //

// TODO: prevent need to use in Main.kt
// TODO: why does this method not throw? (code coverage)

/**
 * Return an [IncorrectTypeException] for when a [YamlNode] is the wrong type.
 *
 * @param expectedType the type that the [YamlNode] should have been
 * @param yamlNode the [YamlNode] that had the wrong type
 * @return the constructed exception
 */
fun incorrectType(expectedType: String, yamlNode: YamlNode): IncorrectTypeException =
  IncorrectTypeException(
    "Expected element to be ${expectedType} but is ${yamlNode::class.simpleName}",
    yamlNode.path,
  )

// ================================================================== //
// Pairs
// ================================================================== //

/**
 * Convert a YAML map containing one entry to a pair of a string label and a
 * YAML node.
 *
 * @receiver the YAML node to convert to a pair
 * @return the pair represented by the YAML node
 * @throw YamlException if the YAML map does not contain exactly one entry
 */
fun YamlMap.toLabeled(): Pair<String, YamlNode> {
  val pair = this.entries.toList().singleOrNull()
    ?: throw YamlException("Expected one map element but found ${this.entries.size}", this.path)
  return pair.first.content to pair.second
}

/**
 * Convert a YAML node to a pair of an optional string label and a YAML node.
 *
 * If the YAML node is a [YamlMap], then it is converted using [toLabeled].
 * Otherwise, a pair of `null` and the YAML node itself is returned.
 *
 * @receiver the YAML node to convert to a pair
 * @return the pair represented by the YAML node
 */
fun YamlNode.toOptionallyLabeled(): Pair<String?, YamlNode> =
  if (this is YamlMap) this.toLabeled() else null to this
