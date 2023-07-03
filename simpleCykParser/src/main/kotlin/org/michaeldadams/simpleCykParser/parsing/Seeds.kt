package org.michaeldadams.simpleCykParser.parsing.seeds

import org.michaeldadams.simpleCykParser.grammar.*
import org.michaeldadams.simpleCykParser.parsing.*

object Seeds {
  fun seeds(grammar: Grammar, nullable: Set<NonTerminal>): Map<Symbol, Set<ParsedProduction>> {
    var partialSymbols: Set<PartiallyParsedProduction> = emptySet()
    for (productions in grammar.parseRules.productions.values) {
      for (production in productions) {
        for (consumed in 0 until production.rhs.size - 1) { // Note use of both `until` and `-1`
          val partialSymbol = PartiallyParsedProduction(production, consumed)
          partialSymbols += partialSymbol

          val nextSymbol = partialSymbol.nextSymbol
          if (nextSymbol !is NonTerminal || nextSymbol !in nullable) { break }
        }
      }
    }

    return partialSymbols.groupBy { it.nextSymbol }.mapValues { it.value.toSet() }
  }
}
