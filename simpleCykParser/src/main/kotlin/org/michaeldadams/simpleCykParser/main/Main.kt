/** Main entry point for simpleCykParser. */

@file:Suppress(
  "UndocumentedPublicClass",
  "UndocumentedPublicProperty",
  "MISSING_KDOC_CLASS_ELEMENTS",
  "MISSING_KDOC_TOP_LEVEL",
)

package org.michaeldadams.simpleCykParser.main

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
import org.michaeldadams.simpleCykParser.grammar.nonterminalNames
import org.michaeldadams.simpleCykParser.grammar.nonterminals
import org.michaeldadams.simpleCykParser.grammar.productionlessNonterminals
import org.michaeldadams.simpleCykParser.grammar.symbols
import org.michaeldadams.simpleCykParser.grammar.undefinedSymbols
import org.michaeldadams.simpleCykParser.grammar.usedSymbols
import org.michaeldadams.simpleCykParser.lexing.lex
import org.michaeldadams.simpleCykParser.parsing.Chart
import org.michaeldadams.simpleCykParser.parsing.ChartEntry
import org.michaeldadams.simpleCykParser.parsing.add
import org.michaeldadams.simpleCykParser.parsing.addUnconsumedItemEntries
import org.michaeldadams.simpleCykParser.yaml.toChartEntries
import org.michaeldadams.simpleCykParser.yaml.toYaml
import org.michaeldadams.simpleCykParser.yaml.toYamlString

// TODO: Chart Entry Type
// TODO: when to do "this."
// TODO: check that all KDoc have @receiver
// TODO: check @throws
// TODO: check for unique terminal and nonterminal names
// TODO: distinguish YAML syntax errors from structure errors

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
  symbols: Set<Symbol>,
  filter: (Symbol) -> Boolean,
): Unit {
  // Empty nonterminals
  for (nonterminal in parseRules.productionlessNonterminals()) {
    echo("Nonterminal with no productions: ${nonterminal}")
  }

  // Unused symbols
  for (symbol in symbols - parseRules.usedSymbols()) {
    echo("Unused symbol: ${symbol}")
  }

  // Undefined nonterminals
  for ((lhs, rhs, index) in parseRules.undefinedSymbols(symbols)) {
    if (filter(rhs.elements[index].symbol)) {
      echo("Undefined symbol ${index} of ${lhs} -> ${rhs}") // TODO
    }
  }

  // TODO: check ParseRules.start is undefined
}

class CheckParseRules : CliktCommand(help = "Perform sanity checks on a parse rules") {
  val parseRules by parseRulesArgument()
  override fun run(): Unit {
    checkImpl(parseRules, parseRules.nonterminals(), { it is Nonterminal })
  }
}

class CheckGrammar : CliktCommand(help = "Perform sanity checks on a grammar") {
  val grammar by grammarArgument()
  override fun run(): Unit {
    // TODO: check overlapping terminals and nonterminals
    checkImpl(grammar.parseRules, grammar.symbols(), { true })
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
  val input: List<Triple<Int, Int, ChartEntry>> by inputArgument().convert { string ->
    tryYaml { string.toYaml().toChartEntries(parseRules.nonterminalNames()) }
  }
  override fun run(): Unit {
    val chart = Chart()
    input.forEach { (start, end, entry) -> chart.add(start, end, entry) }
    chart.addUnconsumedItemEntries(parseRules)
    echo(chart.toYamlString())
  }
}

// TODO: better error message (specify which argument)
// $ ./gradlew installDist && ./simpleCykParser/build/install/simpleCykParser/bin/simpleCykParser parse simpleCykParser/src/test/resources/test.parse <(echo "symbols: 'S'")
// Error: Invalid YAML at symbols on line 1, column 10: Expected element to be YamlList but is YamlScalar

class LexAndParse : CliktCommand(help = "Lex a file then parse the resulting tokens.") {
  val grammar: Grammar by grammarArgument()
  val input: String by inputArgument()
  override fun run(): Unit {
    val (position, tokens) = grammar.lexRules.lex(input)
    if (position != input.length) {
      echo("WARNING: Lexing failed at character ${position}")
    }
    val chart = Chart()
    chart.add(tokens.map { it.terminal })
    chart.addUnconsumedItemEntries(grammar.parseRules)
    echo(chart.toYamlString())
  }
}
