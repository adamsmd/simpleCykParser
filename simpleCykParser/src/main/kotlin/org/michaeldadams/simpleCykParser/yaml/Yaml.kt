/** Functions for converting Yaml to a grammar. */

package org.michaeldadams.simpleCykParser.yaml

import com.charleskorn.kaml.IncorrectTypeException
import com.charleskorn.kaml.MissingRequiredPropertyException
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlPath
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.yamlList
import com.charleskorn.kaml.yamlMap
import com.charleskorn.kaml.yamlScalar
import org.michaeldadams.simpleCykParser.grammar.Grammar
import org.michaeldadams.simpleCykParser.grammar.LexRules
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.RhsElement
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.TerminalRule
import org.michaeldadams.simpleCykParser.lexing.Token
import org.michaeldadams.simpleCykParser.parsing.Chart
import org.michaeldadams.simpleCykParser.parsing.Item
import org.michaeldadams.simpleCykParser.util.toEqRegex
import org.snakeyaml.engine.v2.api.Dump
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.Tag
import java.io.StringWriter
import kotlin.ranges.IntRange
import kotlin.text.MatchGroup
import kotlin.text.toRegex

// TODO: check that all KDoc have @receiver

/**
 * Parses a string as Yaml document.
 *
 * @receiver the string containing the Yaml to parse
 * @return the [YamlNode] resulting from parsing the string
 */
fun String.toYaml(): YamlNode = Yaml.default.parseToYamlNode(this)

/**
 * Extracts lexing rules from a [YamlMap].
 *
 * @receiver TODO
 * @return the lexing rules extracted from the [YamlMap]
 */
fun YamlMap.toLexRules(): LexRules {
  val map = this.toMap()
  val whitespace = map["whitespace", this.path].yamlScalar.content.toRegex().toEqRegex()

  val terminals = map["terminals", this.path].yamlList.items.map { item ->
    val (terminal, regex) = item.yamlMap.toPair()
    TerminalRule(Terminal(terminal), regex.yamlScalar.content.toRegex().toEqRegex())
  }

  // TODO: regex RegexOption.COMMENTS

  return LexRules(whitespace, terminals)
}

/**
 * Extracts parsing rules from a [YamlMap].
 *
 * @receiver TODO
 * @return the parsing rules extracted from the [YamlMap]
 * @throws IncorrectTypeException TODO
 * @throws MissingRequiredPropertyException TODO
 */
fun YamlMap.toParseRules(): ParseRules {
  val map = this.toMap()
  val start: String = map["start", this.path].yamlScalar.content
  val productionsYaml: Map<YamlScalar, YamlNode> = map["productions", this.path].yamlMap.entries
  val nonterminals: Set<String> = productionsYaml.keys.map { it.content }.toSet()

  val productionMap = productionsYaml.map { entry ->
    Nonterminal(entry.key.content) to
      entry.value.yamlList.items.map { it.toRhs(nonterminals) }.toSet()
  }.toMap()

  return ParseRules(Nonterminal(start), productionMap)
}

/**
 * Extracts a grammar from a [YamlMap].
 *
 * @receiver TODO
 * @return the grammar extracted from the [YamlMap]
 */
fun YamlMap.toGrammar(): Grammar = Grammar(this.toLexRules(), this.toParseRules())

// ================================================================== //
// Symbols
// ================================================================== //

private const val TERMINAL_PREFIX = "T:"
private const val NONTERMINAL_PREFIX = "N:"

/**
 * Convert a string into the terminal or nonterminal it represents.
 *
 * If the string starts with "T:", it is a terminal with its name after this prefix.
 * If the string starts with "N:", it is a nonterminal with its name after this prefix.
 * If neither is the case, the string is a nonterminal if it is in [nonterminals].
 * Otherwise, it is a terminal.
 *
 * @receiver the string to be converted to a symbol
 * @param nonterminals the set of strings to treat as nonterminals
 * @return the symbol that the string was converted into
 */
fun String.toSymbol(nonterminals: Set<String>): Symbol =
  when {
    this.startsWith(NONTERMINAL_PREFIX) -> Nonterminal(this.substring(NONTERMINAL_PREFIX.length))
    this.startsWith(TERMINAL_PREFIX) -> Terminal(this.substring(TERMINAL_PREFIX.length))
    this in nonterminals -> Nonterminal(this)
    else -> Terminal(this)
  }

/**
 * Convert a [YamlNode] into the terminal or nonterminal it represents.
 *
 * If the [YamlNode] is a [YamlScalar], [toSymbol] is called on the [content] of
 * that [YamlScalar].  If the [YamlNode is not a [YamlScalar], an exception is
 * thrown.
 *
 * @receiver the [YamlNode] to be converted to a symbol
 * @param nonterminals the set of string to treat as nonterminals
 * @return the symbol that the [YamlNode] was converted into
 */
fun YamlNode.toSymbol(nonterminals: Set<String>): Symbol =
  this.yamlScalar.content.toSymbol(nonterminals)

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Symbol.toYamlString(): String =
  when (this) {
    is Terminal -> (TERMINAL_PREFIX + name).toYamlString()
    is Nonterminal -> (NONTERMINAL_PREFIX + name).toYamlString()
  }

// ================================================================== //
// RhsElement
// ================================================================== //

/**
 * TODO.
 *
 * @receiver TODO
 * @param nonterminals TODO
 * @return TODO
 */
fun YamlNode.toRhsElement(nonterminals: Set<String>): RhsElement {
  val (label, symbol) = this.toOptionalPair()
  return RhsElement(label, symbol.toSymbol(nonterminals))
}

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun RhsElement.toYamlString(): String {
  val symbol = this.symbol.toYamlString()
  return if (this.label == null) symbol else "[${label.toYamlString()}, ${symbol}]"
}

private val WHITESPACE_REGEX = "\\p{IsWhite_Space}+".toRegex()

/**
 * TODO.
 *
 * @receiver TODO
 * @param nonterminals TODO
 * @return TODO
 */
fun YamlNode.toRhsElements(nonterminals: Set<String>): List<RhsElement> =
  when (this) {
    is YamlList -> this.items.map { it.toRhsElement(nonterminals) }
    is YamlScalar -> this.content.split(WHITESPACE_REGEX)
      .filter { it.isNotEmpty() }
      .map { RhsElement(null, it.toSymbol(nonterminals)) }
    else -> throw incorrectType("YamlScalar or YamlList", this)
  }

// ================================================================== //
// Rhs
// ================================================================== //

/**
 * TODO.
 *
 * @receiver TODO
 * @param nonterminals TODO
 * @return TODO
 */
fun YamlNode.toRhs(nonterminals: Set<String>): Rhs {
  val (label, elements) = this.toOptionalPair()
  return Rhs(label, elements.toRhsElements(nonterminals))
}

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Rhs.toYamlString(): String {
  val elements = "[${this.elements.map { "${it.toYamlString()}" }.joinToString() }]"
  return if (this.label == null) elements else "${this.label.toYamlString()}: ${elements}"
}

// ================================================================== //
// Private Helpers
// ================================================================== //

fun incorrectType(expectedType: String, yamlNode: YamlNode): IncorrectTypeException =
  IncorrectTypeException(
    "Expected element to be ${expectedType} but is ${yamlNode::class.simpleName}",
    yamlNode.path,
  )

private operator fun Map<String, YamlNode>.get(key: String, path: YamlPath): YamlNode =
  this[key] ?: throw MissingRequiredPropertyException(key, path)

private fun YamlMap.toMap(): Map<String, YamlNode> = this.entries.mapKeys { it.key.content }

// TODO: rename to toStringPair?
fun YamlMap.toPair(): Pair<String, YamlNode> {
  val pair = this.entries.toList().singleOrNull()
    ?: throw YamlException("Expected one map element but found ${this.entries.size}", this.path)
  return pair.first.content to pair.second
}

fun YamlNode.toOptionalPair(): Pair<String?, YamlNode> =
  if (this is YamlMap) this.toPair() else null to this

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
// Chart
// ================================================================== //

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Item.toYamlString(): String =
  "[${this.lhs.name.toYamlString()}, ${this.rhs.toYamlString()}, ${this.consumed}]"

fun YamlNode.toItem(nonterminals: Set<String>): Item {
  val elements = this.yamlList.items
  if (elements.size != 3) {
    throw YamlException(
      "Items are represented by 3 element lists but found ${elements.size} elements",
      path,
    )
  }
  return Item(
    Nonterminal(elements[0].yamlScalar.content),
    elements[1].toRhs(nonterminals),
    elements[2].yamlScalar.toInt(),
  )
}

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Chart.toYamlString(): String = buildString {
  appendLine("################################################################")
  appendLine("symbols:")
  appendLine()
  for ((start, startValue) in symbols) {
    appendLine("################################")
    appendLine("# start: ${start}")
    for ((end, endValue) in startValue) {
      appendLine("# start: ${start} end: ${end}")
      for (symbol in endValue) {
        appendLine("- [${start}, ${end}, ${symbol.toYamlString()}]")
      }
      appendLine()
    }
  }

  appendLine("################################################################")
  appendLine("items:")
  appendLine()
  for ((start, startValue) in items) {
    appendLine("################################")
    appendLine("# start: ${start}")
    for ((end, endValue) in startValue) {
      appendLine("# start: ${start} end: ${end}")
      for ((item, itemValue) in endValue) {
        for (split in itemValue) {
          appendLine("- [${start}, ${end}, ${item.toYamlString()}, ${split}]")
        }
      }
      appendLine()
    }
  }

  appendLine("################################################################")
  appendLine("symbolEnds:")
  appendLine()
  for ((start, startValue) in symbolEnds) {
    appendLine("################################")
    appendLine("# start: ${start}")
    for ((symbol, symbolValue) in startValue) {
      appendLine("# start: ${start} symbol: ${symbol.toYamlString()}")
      for (end in symbolValue) {
        appendLine("- [${start}, ${symbol.toYamlString()}, ${end}]")
      }
      appendLine()
    }
  }

  appendLine("################################################################")
  appendLine("itemStarts:")
  appendLine()
  for ((end, endValue) in itemStarts) {
    appendLine("################################")
    appendLine("# end: ${end}")
    for ((symbol, symbolValue) in endValue) {
      appendLine("# end: ${end} symbol: ${symbol.toYamlString()}")
      for ((start, startValue) in symbolValue) {
        appendLine("# end: ${end} symbol: ${symbol.toYamlString()} start: ${start}")
        for (item in startValue) {
          appendLine("- [${end}, ${symbol.toYamlString()}, ${start}, ${item.toYamlString()}]")
        }
        appendLine()
      }
    }
  }
}

// ================================================================== //
// Token
// ================================================================== //

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun IntRange.toYamlString(): String = "[${start}, ${endInclusive}]"

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun MatchGroup?.toYamlString(): String =
  if (this == null) "null" else "${value.toYamlString()}: ${range.toYamlString()}"

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Token.toYamlString(): String =
  "${terminal.name.toYamlString()}: [${groups.map { it.toYamlString() }.joinToString()}]"

//  "A": ["xxx": [12,15], "xx": [13,14]]
//  "A": ["xxx": [12,15], [], "xx": [13,14]]
//  "A": [["xxx", 12,15], "xx": [13,14]]
