/** TODO: Helpers for main commands. */

package org.michaeldadams.simpleCykParser.main

import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlMap
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.types.inputStream
import org.michaeldadams.simpleCykParser.grammar.Grammar
import org.michaeldadams.simpleCykParser.grammar.LexRules
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.yaml.toGrammar
import org.michaeldadams.simpleCykParser.yaml.toLexRules
import org.michaeldadams.simpleCykParser.yaml.toParseRules
import org.michaeldadams.simpleCykParser.yaml.toYamlMap
import java.io.InputStream

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun CliktCommand.inputArgument(): ProcessedArgument<String, String> =
  argument(help = "File containing the grammar").inputStream().convert { it.reader().readText() }

fun <S, T> S.tryYaml(action: S.() -> T): T =
  try {
    this.action()
  } catch (e: YamlException) {
    // TODO: InvalidFileFormat(filename, message, lineno)
    // Error: incorrect format in file filename line 10: message
    throw UsageError(
      "Invalid YAML at ${e.path.toHumanReadableString()} " +
        "on line ${e.line}, column ${e.column}: ${e.message}",
    )
  }

/**
 * TODO.
 *
 * @receiver TODO
 * @param T TODO
 * @param help TODO
 * @param converter TODO
 * @return TODO
 */
fun <T : Any> CliktCommand.rulesArgument(help: String, converter: (YamlMap) -> T):
  ProcessedArgument<T, T> =
  this.argument(help = help).inputStream().convert<InputStream, T> {
    // TODO: use tryYaml and check that error message is same
    try {
      converter(it.reader().readText().toYamlMap())
    } catch (e: YamlException) {
      // TODO: InvalidFileFormat(filename, message, lineno)
      fail(
        "Invalid YAML at ${e.path.toHumanReadableString()} " +
          "on line ${e.line}, column ${e.column}: ${e.message}",
      )
    }
  }

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun CliktCommand.lexRulesArgument(): ProcessedArgument<LexRules, LexRules> =
  rulesArgument("File containing the lexer rules. Use '-' for stdin.") { it.toLexRules() }

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun CliktCommand.parseRulesArgument(): ProcessedArgument<ParseRules, ParseRules> =
  rulesArgument("File containing the parser rules. Use '-' for stdin.") { it.toParseRules() }

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun CliktCommand.grammarArgument(): ProcessedArgument<Grammar, Grammar> =
  rulesArgument("File containing the grammar. Use '-' for stdin.") { it.toGrammar() }
