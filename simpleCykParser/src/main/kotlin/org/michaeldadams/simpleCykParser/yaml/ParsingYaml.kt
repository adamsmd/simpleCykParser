/** Functions for converting Yaml to a grammar. */

package org.michaeldadams.simpleCykParser.yaml

import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.yamlList
import com.charleskorn.kaml.yamlScalar
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.parsing.Chart
import org.michaeldadams.simpleCykParser.parsing.ChartEntry
import org.michaeldadams.simpleCykParser.parsing.Item
import org.michaeldadams.simpleCykParser.parsing.ItemEntry
import org.michaeldadams.simpleCykParser.parsing.SymbolEntry

// ================================================================== //
// Item
// ================================================================== //

/**
 * Convert a [Item] object to its YAML representation.
 *
 * @receiver the object to convert to YAML
 * @return the YAML resulting from converting the object
 */
fun Item.toYamlString(): String =
  "[${this.lhs.name.toYamlString()}, ${this.rhs.toYamlString()}, ${this.consumed}]"

/**
 * Convert a YAML node to an [Item] object.
 *
 * @receiver the YAML node to convert to an object
 * @param nonterminals the strings to be treated as names of nonterminals
 * @return the object represented by the YAML node
 * @throws YamlException if the YAML node has an unexpected structure
 */
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

// ================================================================== //
// ChartEntry
// ================================================================== //

// TODO: document
fun YamlNode.toChartEntry(pos: Int, nonterminals: Set<String>): Triple<Int, Int, ChartEntry> =
  when (this) {
    is YamlScalar ->
      // Symbol at next position
      Triple(pos, pos + 1, SymbolEntry(this.toSymbol(nonterminals)))
    is YamlMap ->
      // Symbol at next position
      Triple(pos, pos + 1, SymbolEntry(this.toLabeled().first.toSymbol(nonterminals)))
    is YamlList ->
      when (this.items.size) {
        // Symbol at specified position
        3 -> {
          val (start, end, symbol) = this.items
          Triple(
            start.yamlScalar.toInt(),
            end.yamlScalar.toInt(),
            SymbolEntry(symbol.toSymbol(nonterminals)),
          )
        }
        // Item at specified position
        4 -> {
          val (start, end, item, split) = this.items
          Triple(
            start.yamlScalar.toInt(),
            end.yamlScalar.toInt(),
            ItemEntry(item.toItem(nonterminals), split.toIntOrNull()),
          )
        }
        else -> {
          throw YamlException(
            "Expected 3 (for symbols) or 4 (for items) list elements but found ${this.items.size}",
            this.path,
          )
        }
      }
    else -> throw incorrectType("YamlScalar or YamlMap or YamlList", this)
  }

// TODO: document
fun YamlList.toChartEntries(nonterminals: Set<String>): List<Triple<Int, Int, ChartEntry>> =
  this.items.scan(Triple(0, 0, SymbolEntry(Nonterminal("")) as ChartEntry)) { acc, yamlNode ->
    yamlNode.toChartEntry(acc.second, nonterminals)
  }.drop(1)

// TODO: document
fun YamlNode.toChartEntries(nonterminals: Set<String>): List<Triple<Int, Int, ChartEntry>> =
  when (this) {
    is YamlList -> listOf(this)
    is YamlMap ->
      // TODO: constant for "symbols"
      listOf("tokens", "symbols", "items")
        .mapNotNull<String, YamlList> { get<YamlNode>(it)?.yamlList }
      // TODO: check that list is not empty
    else -> throw incorrectType("YamlMap or YamlList", this)
  }.flatMap { it.toChartEntries(nonterminals) }

// ================================================================== //
// Chart
// ================================================================== //

/**
 * Convert a [Chart] object to its YAML representation.
 *
 * @receiver the object to convert to YAML
 * @return the YAML resulting from converting the object
 */
fun Chart.toYamlString(): String = buildString {
  // TODO: .sorted()
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
  appendLine("nextItems:")
  appendLine()
  for ((end, endValue) in nextItems) {
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
