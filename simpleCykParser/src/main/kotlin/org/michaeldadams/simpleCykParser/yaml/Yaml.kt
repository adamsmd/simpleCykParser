/** Functions for converting Yaml to a grammar. */

package org.michaeldadams.simpleCykParser.yaml

import com.charleskorn.kaml.IncorrectTypeException
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import org.snakeyaml.engine.v2.api.Dump
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.Tag
import java.io.StringWriter

// TODO: check that all KDoc have @receiver

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
// Strings
// ================================================================== //

// TODO: broken on "". produces `--- ""`
// val yaml = Yaml(configuration=YamlConfiguration(singleLineStringStyle=SingleLineStringStyle.Plain))
// fun String.toYamlString(): String = "\"" + yaml.encodeToString(String.serializer(), this) + "\""
// fun String.toYamlString(): String = Yaml.default.encodeToString(String.serializer(), this)

fun String.toYamlString(): String {
  // Other potentially useful settings are setWidth() and setSplitLines()
  val writer = StreamToStringWriter()
  Dump(DumpSettings.builder().setUseUnicodeEncoding(false).setBestLineBreak("").build())
    .dumpNode(ScalarNode(Tag.STR, this, ScalarStyle.DOUBLE_QUOTED), writer)
  return writer.toString()
}

private class StreamToStringWriter : StringWriter(), StreamDataWriter {
  override fun flush(): Unit = super<StringWriter>.flush()
}

// ================================================================== //
// Type Error
// ================================================================== //

// TODO: prevent need to use in Main.kt

fun incorrectType(expectedType: String, yamlNode: YamlNode): IncorrectTypeException =
  IncorrectTypeException(
    "Expected element to be ${expectedType} but is ${yamlNode::class.simpleName}",
    yamlNode.path,
  )

// ================================================================== //
// Pairs
// ================================================================== //

// TODO: rename to toStringPair?
fun YamlMap.toPair(): Pair<String, YamlNode> {
  val pair = this.entries.toList().singleOrNull()
    ?: throw YamlException("Expected one map element but found ${this.entries.size}", this.path)
  return pair.first.content to pair.second
}

fun YamlNode.toOptionalPair(): Pair<String?, YamlNode> =
  if (this is YamlMap) this.toPair() else null to this
