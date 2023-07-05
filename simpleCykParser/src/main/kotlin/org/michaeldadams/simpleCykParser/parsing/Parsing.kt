package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.collections.AutoMap
import org.michaeldadams.simpleCykParser.collections.QueueSet
import org.michaeldadams.simpleCykParser.grammar.Grammar
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol

// sealed interface ParsedProduction
// data class CompletelyParsedProduction(val production: Production) : ParsedProduction
// data class PartiallyParsedProduction(val production: Production, val consumed: Int) : ParsedProduction {
//   init {
//     require(consumed < production.rhs.size) {
//       "Consumed is greater than or equal to production length: ${this}"
//     }
//   }
//   val nextSymbol: Symbol
//     get() = production.rhs[consumed]

//   val nextParsedProduction: ParsedProduction
//     get() =
//       if (this.consumed == this.production.rhs.size - 1) {
//         CompletelyParsedProduction(production)
//       } else {
//         PartiallyParsedProduction(production, consumed + 1)
//       }
// }

// nt -> prod | prod | prod
// prod -> prod/2 prod.2
// prod/2 -> prod/1 prod.1
// prod/1 -> prod/0 prod.0
// prod/0 -> epsilon

// sealed interface P(val symbol: Symbol, val production: Production?, val )
// data class Asserted(val symbol: Symbol) : P
// data class Constructed(val production: Production, val consumed: Int) {
//   override val symbol: Symbol get() = production.lhs
// }
// sealed interface ChartEntry
// data class SymbolEntry(Symbol)
// data class ProductionEntry(Production, consumed)

// fun Production.toCompletePartialProduction
// fun Production.toInitialPartialProduction
// toPartial(0)
// toPartial(1)
// toPartial(-1)

data class PartialProduction(val production: Production, val consumed: Int) {
  val isComplete: Boolean
    get() = TODO()
  fun next(): PartialProduction? = TODO()
  val lastConsumed: Symbol = TODO()
}
// TODO: lastPartial?
fun Production.toPartial(consumed: Int): PartialProduction {
  require(consumed >= 0)
  require(consumed <= this.rhs.size)
  return PartialProduction(this, consumed)
}

class ProcessedGrammar(val grammar: Grammar) {
  val nullable: Set<Production> = TODO()
  val initialUses: Map<Symbol, Set<Production>> = TODO()
}

// TODO: move to Chart.kt
class Chart(val grammar: ProcessedGrammar, val size: Int) {
  // get left
  // get right
  // get children
  // get parses at
   // TODO: initialize all tables with nullable

  val symbols = Symbols()
  inner class Symbols {
    private val keys: AutoMap<Int, AutoMap<Int, QueueSet<Symbol>>> =
      AutoMap { AutoMap { QueueSet() } }
    private val entries: AutoMap<Int, AutoMap<Int, AutoMap<Symbol, QueueSet<Production?>>>> =
      AutoMap { AutoMap { AutoMap { QueueSet() } } }
    private val ends: AutoMap<Int, AutoMap<Symbol, QueueSet<Int>>> =
      AutoMap { AutoMap { QueueSet() } }

    // Used to find parses
    operator fun get(start: Int, end: Int): Set<Symbol> = keys[start][end]
    // TODO: investigate getOrImplicitDefault (implementation of getValue)
    // Used to get productions for parses
    operator fun get(start: Int, end: Int, symbol: Symbol): Set<Production?> = entries[start][end][symbol]
    // Used to get 'rightEnd'
    operator fun get(start: Int, symbol: Symbol): Set<Int> = ends[start][symbol]
      // production.toCompletePartialProduction
    operator fun plusAssign(entry: Pair<Pair<Int, Int>, Pair<Symbol, Production?>>): Unit {
      val (start, end) = entry.first
      val (symbol, production) = entry.second
      // val ((start, end), (symbol, production)) = entry // TODO: in argument?
      // NOTE: just an assertion and does not show how built
      // TODO: add always adds ProductionEntry (via initialUses)
      // chart.symbols += Pair(start, end) to Pair(symbol, production)
      if (production !in entries[start][end][symbol]) {
        keys[start][end] += symbol
        entries[start][end][symbol] += production
        ends[start][symbol] += end
        for (newProduction in grammar.initialUses[symbol]!!) { // TODO: handle null
          productions += Pair(start, end) to Pair(newProduction.toPartial(1), start)
        }
      }
    }
  }

  val productions = Productions()
  inner class Productions {
    private val keys: AutoMap<Int, AutoMap<Int, QueueSet<PartialProduction>>> =
      AutoMap { AutoMap { QueueSet() } }
    private val entries: AutoMap<Int, AutoMap<Int, AutoMap<PartialProduction, QueueSet<Int?>>>> =
      AutoMap { AutoMap { AutoMap { QueueSet() } } }
    private val ends: AutoMap<Int, AutoMap<PartialProduction, QueueSet<Int>>> =
      AutoMap { AutoMap { QueueSet() } }

    // Used to get 'leftChild'
    operator fun get(start: Int, end: Int): QueueSet<PartialProduction> = keys[start][end]
    // Used to get division between children
    operator fun get(start: Int, end: Int, partial: PartialProduction): QueueSet<Int?> = entries[start][end][partial]
    // Not used
    operator fun get(start: Int, partial: PartialProduction): QueueSet<Int> = ends[start][partial]
    operator fun plusAssign(entry: Pair<Pair<Int, Int>, Pair<PartialProduction, Int?>>): Unit {
      val (start, end) = entry.first
      val (partial, previous) = entry.second // TODO: in argument?
      // TODO: add always adds Symbol of the partialProd is complete
      if (previous !in entries[start][end][partial]) {
        keys[start][end] += partial
        entries[start][end][partial] += previous
        ends[start][partial] += end
        if (partial.isComplete) {
          symbols += Pair(start, end) to Pair(partial.production.lhs, partial.production)
        }
      }
    }
  }
}

// toPartial: Productions are unchanged, Symbols get their initial use Productions

fun parse(chart: Chart) {
  for (leftEnd in chart.size..0) {
    val rightStart = leftEnd
    for (leftStart in 0..leftEnd) {
      // gets new elements if rightChild is nulled
      for (leftChild in chart.productions[leftStart, leftEnd]) {
        leftChild.next()?.let { nextPartial ->
          // gets new elements if leftChild is nulled
          for (rightEnd in chart.symbols[rightStart, nextPartial.lastConsumed]) {
            chart.productions += Pair(leftStart, rightEnd) to Pair(nextPartial, leftEnd)
          }
        }
      }
    }
  }
}

// data class Chart(
//   /**
//    * Table of complete parses.  Indexed by:
//    *  - row number / parse start position
//    *  - production lhs / nonterminal
//    *  - column number / parse end position
//    *  - production / complete symbol
//    *  - a set of indexes one step back in the parse
//    */
//   val byRowAndSymbol:
//     AutoMap<Int, AutoMap<Symbol, AutoMap<Int, AutoMap<Production, MutableSet<Int>>>>> =
//       AutoMap { AutoMap { AutoMap { AutoMap { mutableSetOf() } } } },

//   /**
//    * Table of partial parses.  Indexed by:
//    *  - row number / parse start position
//    *  - column number / parse end position
//    *  - partially parsed production / partial symbol
//    *  - a set of indexes one step back in the parse
//    */
//   val byRowAndCol:
//     AutoMap<Int, AutoMap<Int, AutoMap<PartiallyParsedProduction, MutableSet<Int>>>> =
//       AutoMap { AutoMap { AutoMap { mutableSetOf() } } }
// )
//       // class Chart {
// //   static fun fromTokens
// //   static fun fromSymbols
// // }

// // class Parser(productions) {
// //   val seeds
// //   val nullable
// //   fun parse(table)
// //   fun parseFromTokens
// //   fun parse
// // }

// class PartialParseTable {
//   /** Which symbols are nullable. */
//   val nullable: Set<Symbol> = TODO()

//   /** Partial symbols that should be generated if a symbol is completely parsed. */
//   // TODO: rename to "partial" or something "partialProductionInitializers"
//   val seeds: Map<Symbol, Set<PartiallyParsedProduction>> = TODO()

//   /**
//    * Table of complete parses.  Indexed by:
//    *  - row number / parse start position
//    *  - production lhs
//    *  - column number / parse end position
//    *  - production / complete symbol
//    *  - a set of indexes one step back in the parse
//    */
//   val byRowAndSymbol:
//     AutoMap<Int, AutoMap<Symbol, AutoMap<Int, AutoMap<Production, MutableSet<Int>>>>> =
//       AutoMap { AutoMap { AutoMap { AutoMap { mutableSetOf() } } } }

//   /**
//    * Table of partial parses.  Indexed by:
//    *  - row number / parse start position
//    *  - column number / parse end position
//    *  - partially parsed production / partial symbol
//    *  - a set of indexes one step back in the parse
//    */
//   val byRowAndCol:
//     AutoMap<Int, AutoMap<Int, AutoMap<PartiallyParsedProduction, MutableSet<Int>>>> =
//       AutoMap { AutoMap { AutoMap { mutableSetOf() } } }

//   // TODO: function to check consistency of parse chart (takes set of null?)

//   // TODO: note that "null" parses are not stored in the table

//   /**
//    * Take a parse (left) that goes from leftRow to middle, and add an entry that steps forward one and goes from leftRow to rightCol.
//    */
//   fun add(left: PartiallyParsedProduction, leftRow: Int, middle: Int, rightCol: Int): Unit {
//     when (val parsedProduction = left.nextParsedProduction) {
//       is CompletelyParsedProduction ->
//         if (byRowAndSymbol[leftRow][left.production.lhs][rightCol][parsedProduction.production]
//           .add(middle)
//         ) {
//           // TODO: stop if nothing new added (for support of non-expanding productions)
//           // complete entry causes partial entry (and empties before and after that if needed)
//           for (i in seeds[left.production.lhs] ?: emptySet()) {
//             add(i, leftRow, leftRow, rightCol)
//           }
//         }
//       is PartiallyParsedProduction ->
//         if (byRowAndCol[leftRow][middle][parsedProduction].add(rightCol)) {
//           // TODO: stop if nothing new added (for support of non-expanding productions)
//           if (parsedProduction.nextSymbol in nullable) {
//             add(parsedProduction, leftRow, rightCol, rightCol)
//           }
//         }
//     }
//   }
// }

// object Parser {
//   // Assumes tokens have already been added
//   fun parse(table: PartialParseTable): Unit {
//     // TODO: proactive add on existing entries

//     for ((leftRowIndex, leftRowMap) in ReverseNavigableIterator(table.byRowAndCol.map)) {
//       for ((leftColIndex, leftEntry) in NavigableIterator(leftRowMap.map)) {
//         for ((leftSymbol, _) in NavigableIterator(leftEntry.map)) { // QueueIterator?
//           val rightSymbol = leftSymbol.nextSymbol // populated with initializers?
//           // val rightSymbol, nextProd = leftSymbol.follows
//           for ((rightColIndex, _) in NavigableIterator(table.byRowAndSymbol[leftColIndex][rightSymbol].map)) {
//             // NOTE: use a proactive add instead of a reactive add
//             table.add(leftSymbol, leftRowIndex, leftColIndex, rightColIndex)
//           }
//         }
//       }
//     }
//   }
// }
