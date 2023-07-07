/** Main entry point for simpleCykParser. */

package org.michaeldadams.simpleCykParser.main

import com.github.ajalt.clikt.completion.completionOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple

/** Runs the main entry point of the application. */
fun main(args: Array<String>): Unit = Main().main(args)

/** Returns the class of the entry point of the application. */
@Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
val mainClass: Class<*> = object {}.javaClass.enclosingClass

/** The main application. */
@Suppress("TrimMultilineRawString", "UndocumentedPublicProperty", "MISSING_KDOC_CLASS_ELEMENTS")
class Main : CliktCommand(
  name = "simpleCykParser",
  printHelpOnEmptyArgs = true,
  help = """
    Complete But Simple-To-Implement CYK Parsing.

    See https://github.com/adamsmd/simpleCykParser/README.md for more details.
    """
) {
  val arg: List<String> by argument(
    help = """
      TODO
      """
  ).multiple()

  init {
    context { helpFormatter = CliktHelpFormatter(showDefaultValues = true) }
    completionOption(help = "Generate an autocomplete script for the given shell")
  }

  override fun run(): Unit {
    // TODO: warn if no args

    // print grammar
    // check grammar
    // lex
    // parse (table as yaml)
  }
}
