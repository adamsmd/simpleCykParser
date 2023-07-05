package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Grammar
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol

// TODO: PartialProductionInitializers
// fun seeds(grammar: Grammar, nullable: Set<Nonterminal>): Map<Symbol, Set<ParsedProduction>> {
//   var partialSymbols: Set<PartiallyParsedProduction> = emptySet()
//   for (productions in grammar.parseRules.productions.values) {
//     for (production in productions) {
//       for (consumed in 0 until production.rhs.size - 1) { // Note use of both `until` and `-1`
//         val partialSymbol = PartiallyParsedProduction(production, consumed)
//         partialSymbols += partialSymbol

//         val nextSymbol = partialSymbol.nextSymbol
//         if (nextSymbol !is Nonterminal || nextSymbol !in nullable) { break }
//         // TODO: handle fully parsed productions
//       }
//     }
//   }

//   return partialSymbols.groupBy { it.nextSymbol }.mapValues { it.value.toSet() }
// }

fun initialUses(grammar: Grammar): Map<Symbol, Set<Production>> =
  grammar.parseRules.productions.values.flatten()
    .filter { it.rhs.isNotEmpty() }
    .groupBy { it.rhs.first() }
    .mapValues { it.value.toSet() }
