/** Functions for converting Yaml to a grammar. */

package org.michaeldadams.simpleCykParser.yaml

import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.yamlList
import com.charleskorn.kaml.yamlScalar
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.parsing.Chart
import org.michaeldadams.simpleCykParser.parsing.Item

// ================================================================== //
// Item
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

// ================================================================== //
// Chart
// ================================================================== //

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
