/** Main entry point for simpleCykParser. */

@file:Suppress(
  "UndocumentedPublicClass",
  "UndocumentedPublicProperty",
  "MISSING_KDOC_CLASS_ELEMENTS",
  "MISSING_KDOC_TOP_LEVEL",
)

package org.michaeldadams.simpleCykParser.main

import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.yamlList
import com.charleskorn.kaml.yamlScalar
import com.github.ajalt.clikt.completion.completionOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.InvalidFileFormat
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.versionOption
import org.michaeldadams.simpleCykParser.BuildInformation
import org.michaeldadams.simpleCykParser.grammar.Grammar
import org.michaeldadams.simpleCykParser.grammar.LexRules
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.definedNonterminals
import org.michaeldadams.simpleCykParser.grammar.definedSymbols
import org.michaeldadams.simpleCykParser.grammar.productionlessNonterminals
import org.michaeldadams.simpleCykParser.grammar.undefinedSymbols
import org.michaeldadams.simpleCykParser.grammar.usedSymbols
import org.michaeldadams.simpleCykParser.lexing.lex
import org.michaeldadams.simpleCykParser.parsing.Chart
import org.michaeldadams.simpleCykParser.yaml.incorrectType
import org.michaeldadams.simpleCykParser.yaml.toItem
import org.michaeldadams.simpleCykParser.yaml.toLabeled
import org.michaeldadams.simpleCykParser.yaml.toSymbol
import org.michaeldadams.simpleCykParser.yaml.toYaml
import org.michaeldadams.simpleCykParser.yaml.toYamlString

// TODO: Chart Entry Type
// TODO: when to do "this."
// TODO: check that all KDoc have @receiver
// TODO: check @throws
// TODO: check for unique terminal and nonterminal names

/** The main entry point of the application.
 *
 * @param args TODO
 */
fun main(args: Array<String>): Unit = Main().subcommands(
  PrintLexRules(),
  PrintParseRules(),
  PrintGrammar(),
  CheckParseRules(),
  CheckGrammar(),
  Lex(),
  Parse(),
  LexAndParse(),
).main(args)

class Main : CliktCommand(
  name = "simpleCykParser",
  printHelpOnEmptyArgs = true,
  help = """
    Complete But Simple-To-Implement CYK Parsing.

    See https://github.com/adamsmd/simpleCykParser/README.md for more details.
  """.trimIndent(),
) {
  init {
    context {
      helpFormatter = { MordantHelpFormatter(it, showDefaultValues = true, showRequiredTag = true) }
    }
    completionOption(help = "Generate a tab-complete script for the given shell")
    versionOption(BuildInformation.VERSION)
  }
  override fun run(): Unit = Unit
}

// ================================================================== //
// Printing
// ================================================================== //

class PrintLexRules : CliktCommand(help = "Print the internal representation of lex rules") {
  val lexRules by lexRulesArgument()
  override fun run(): Unit {
    echo(lexRules)
  }
}

class PrintParseRules : CliktCommand(help = "Print the internal representation of parse rules") {
  val parseRules by parseRulesArgument()
  override fun run(): Unit {
    echo(parseRules)
  }
}

class PrintGrammar : CliktCommand(help = "Print the internal represention of a grammar") {
  val grammar by grammarArgument()
  override fun run(): Unit {
    echo(grammar)
  }
}

// ================================================================== //
// Checking
// ================================================================== //

/**
 * Shared implementation for [CheckParseRules] and [CheckGrammar].
 *
 * @param parseRules the [ParseRules] used in the checks
 * @param definedSymbols the symbols to treat as defined
 * @param filter filter determining which symbols to report as undefined
 */
fun CliktCommand.checkImpl(
  parseRules: ParseRules,
  definedSymbols: Set<Symbol>,
  filter: (Symbol) -> Boolean,
): Unit {
  // Empty nonterminals
  for (nonterminal in parseRules.productionlessNonterminals()) {
    echo("Nonterminal with no productions: ${nonterminal}")
  }

  // Unused symbols
  for (symbol in definedSymbols - parseRules.usedSymbols()) {
    echo("Unused symbol: ${symbol}")
  }

  // Undefined nonterminals
  for ((lhs, rhs, index) in parseRules.undefinedSymbols(definedSymbols)) {
    if (filter(rhs.elements[index].symbol)) {
      echo("Undefined symbol ${index} of ${lhs} -> ${rhs}") // TODO
    }
  }

  // TODO: check ParseRules.start is undefined
}

class CheckParseRules : CliktCommand(help = "Perform sanity checks on a parse rules") {
  val parseRules by parseRulesArgument()
  override fun run(): Unit {
    checkImpl(parseRules, parseRules.definedNonterminals(), { it is Nonterminal })
  }
}

class CheckGrammar : CliktCommand(help = "Perform sanity checks on a grammar") {
  val grammar by grammarArgument()
  override fun run(): Unit {
    // TODO: check overlapping terminals and nonterminals
    checkImpl(grammar.parseRules, grammar.definedSymbols(), { true })
  }
}

// ================================================================== //
// Lexing
// ================================================================== //

class Lex : CliktCommand(help = "Tokenize/lex a file") {
  val lexRules: LexRules by lexRulesArgument()
  val input: String by inputArgument()
  override fun run(): Unit {
    val (position, tokens) = lexRules.lex(input)
    // throw ProgramResult(1)
    echo("inputLength: ${input.length}")
    echo("tokenizedLength: ${position}")
    echo("tokens:")
    for (token in tokens) {
      echo("- ${token.toYamlString()}")
    }
  }
}

// TODO: the following reads all of stdin before reporting the error
// ./simpleCykParser/build/install/simpleCykParser/bin/simpleCykParser lex <(echo "whitespace") -

// ================================================================== //
// Parsing
// ================================================================== //

/**
 * TODO.
 *
 * @receiver TODO
 * @param chart TODO
 */
fun YamlList.addListTo(chart: Chart): Unit {
  var pos = 0
  val nonterminals = chart.parseRules.productionMap.keys.map { it.name }.toSet()
  for (node in this.items) {
    // TODO: tryYaml
    when (node) {
      is YamlScalar -> {
        // Add Symbol at next position
        chart.add(pos, pos + 1, node.toSymbol(nonterminals))
        pos++
      }
      is YamlMap -> {
        // Add Symbol at next position
        chart.add(pos, pos + 1, node.toLabeled().first.toSymbol(nonterminals))
        pos++
      }
      is YamlList -> {
        when (node.items.size) {
          // Add Symbol at specified position
          3 -> chart.add(
            node.items[0].yamlScalar.toInt(),
            node.items[1].yamlScalar.toInt().also { pos = it },
            node.items[2].toSymbol(nonterminals),
          )
          // Add Item
          4 -> chart.add(
            node.items[0].yamlScalar.toInt(),
            node.items[1].yamlScalar.toInt().also { pos = it },
            node.items[2].toItem(nonterminals),
            node.items[3].yamlScalar.toInt(),
          )
          else -> throw YamlException(
            "Expected 3 (for symbols) or 4 (for items) elements in list but found ${
              node.items.size}",
            node.path,
          )
        }
      }
      else -> {
        throw incorrectType("YamlScalar or YamlMap or YamlList", node)
      }
    }
    // throw InvalidFileFormat("filename", "message", 10)
  }
}

class Parse : CliktCommand(
  help = """
    Parse a sequence of symbols.

    If input is a map, then "tokens", "items" and "symbols" are consulted for lists.
    If input is a list, then the list is consulted.

    In the list:
    - A string is a symbol (starting where the last symbol ended)
    - A pair is a symbol (starting where the last symbol ended)
    - A triple is an item
    - [start, end, symbol]
    - [start, end, item, split]
    TODO: Tokens are expected either as "A" or "A: " and may be property of "tokens" or "terminals"
    field of map.  Or Chart-style map.  Supports both terminals and nonterminals.
    // map -> try tokens and terminals (start where previous ended) and items/symbols
    // list -> "A" or "A: "
  """.trimIndent(),
) {
  // TODO: val symbols by option(default = true)
  // TODO: val items by option by option()
  // TODO: val symbolStarts by option()
  // TODO: val itemEnds by option()
  // TODO: include these in LexAndParse
  val parseRules: ParseRules by parseRulesArgument()
  val input: List<YamlList> by inputArgument().convert { string ->
    tryYaml {
      val yaml = string.toYaml()
      if (yaml is YamlMap) {
        // TODO: constant for "symbols"
        listOf("symbols").mapNotNull<String, YamlList> { yaml.get<YamlNode>(it)?.yamlList }
        // TODO: check that list is not empty
      } else {
        listOf(yaml.yamlList)
      }
    }
  }
  override fun run(): Unit {
    val chart = Chart(parseRules)
    input.forEach { it.addListTo(chart) }
    chart.addEpsilonItems()
    echo(chart.toYamlString())
  }
}

class LexAndParse : CliktCommand(help = "Lex a file then parse the resulting tokens.") {
  val grammar: Grammar by grammarArgument()
  val input: String by inputArgument()
  override fun run(): Unit {
    val (position, tokens) = grammar.lexRules.lex(input)
    if (position != input.length) {
      echo("WARNING: Lexing failed at character ${position}")
    }
    val chart = Chart(grammar.parseRules)
    chart.add(tokens.map { it.terminal })
    chart.addEpsilonItems()
    echo(chart.toYamlString())
  }
}
