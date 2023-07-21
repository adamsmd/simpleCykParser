/** Main entry point for simpleCykParser. */

@file:Suppress(
  "UndocumentedPublicClass",
  "UndocumentedPublicProperty",
  "MISSING_KDOC_CLASS_ELEMENTS",
  "MISSING_KDOC_TOP_LEVEL",
)

package org.michaeldadams.simpleCykParser.main

import com.charleskorn.kaml.yamlList
import com.github.ajalt.clikt.completion.completionOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.MordantHelpFormatter
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
import org.michaeldadams.simpleCykParser.yaml.toYaml
import org.michaeldadams.simpleCykParser.yaml.toYamlString

/** Runs the main entry point of the application. */
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

/** The main application. */
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
      echo("Undefined symbol ${index} of ${lhs} -> ${rhs}")
    }
  }
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

    TODO: Tokens are expected either as "A" or "A: " and may be property of "tokens" or "terminals"
    field of map.  Or Chart-style map.  Supports both terminals and nonterminals.
  """.trimIndent(),
) {
  val parseRules: ParseRules by parseRulesArgument()
  val input: String by inputArgument()
  override fun run(): Unit {
    // TODO: automatically try map or list
    val terminals = input.toYaml().yamlList.items.map { TODO() }
    val chart = Chart(parseRules)
    chart.add(terminals)
    chart.addEpsilonItems()
    echo(chart.toYamlString())
  }
}

class LexAndParse : CliktCommand(
  help = """
    Lex a file then parse the resulting tokens.

    TODO: Tokens are expected either as "A" or "A: " and may be property of "tokens" or "terminals"
    field of map.  Or Chart-style map.
  """.trimIndent(),
) {
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
