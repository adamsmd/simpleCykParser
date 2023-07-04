package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.*
import org.michaeldadams.simpleCykParser.collections.defMap.*
import org.michaeldadams.simpleCykParser.collections.iterators.*

sealed interface ParsedProduction
data class CompleteSymbol(val production: Production) : ParsedProduction
data class PartiallyParsedProduction(val production: Production, val consumed: Int) : ParsedProduction {
  init {
    assert(consumed < production.rhs.size) {
      "Consumed is greater than or equal to production length: ${this}"
    }
  }
  val nextSymbol: Symbol
    get() = production.rhs[consumed]

  fun nextParsedProduction(): ParsedProduction =
    if (this.consumed == this.production.rhs.size - 1) {
      CompleteSymbol(production)
    } else {
      PartiallyParsedProduction(production, consumed + 1)
    }
}

// class Chart {
//   static fun fromTokens
//   static fun fromSymbols
// }

// class Parser(productions) {
//   val seeds
//   val nullable
//   fun parse(table)
//   fun parseFromTokens
//   fun parse
// }

class PartialParseTable {
  /** Which symbols are nullable */
  val nullable: Set<Symbol> = TODO()

  /** Partial symbols that should be generated if a symbol is completely parsed */
  // TODO: rename to "partial" or something "partialProductionInitializers"
  val seeds: Map<Symbol, Set<PartiallyParsedProduction>> = TODO()

  /**
   * Table of complete parses.  Indexed by:
   *  - row number / parse start position
   *  - production lhs
   *  - column number / parse end position
   *  - production / complete symbol
   *  - a set of indexes one step back in the parse
   */
  val byRowAndSymbol:
    DefTreeMap<Int, DefTreeMap<Symbol, DefTreeMap<Int, DefTreeMap<Production, MutableSet<Int>>>>> =
      defTreeMap { defTreeMap { defTreeMap { defTreeMap { mutableSetOf() } } } }

  /**
   * Table of partial parses.  Indexed by:
   *  - row number / parse start position
   *  - column number / parse end position
   *  - partially parsed production / partial symbol
   *  - a set of indexes one step back in the parse
   */
  val byRowAndCol:
    DefTreeMap<Int, DefTreeMap<Int, DefTreeMap<PartiallyParsedProduction, MutableSet<Int>>>> =
      defTreeMap { defTreeMap { defTreeMap { mutableSetOf() } } }

  // TODO: function to check consistency of parse chart (takes set of null?)

  // TODO: note that "null" parses are not stored in the table

  /**
   * Take a parse (left) that goes from leftRow to middle, and add an entry that steps forward one and goes from leftRow to rightCol.
   */
  fun add(left: PartiallyParsedProduction, leftRow: Int, middle: Int, rightCol: Int): Unit {
    when (val parsedProduction = left.nextParsedProduction()) {
      is CompleteSymbol -> {
        if (byRowAndSymbol[leftRow][left.production.lhs][rightCol][parsedProduction.production].add(middle)) {
          // TODO: stop if nothing new added (for support of non-expanding productions)
          // complete entry causes partial entry (and empties before and after that if needed)
          for (i in seeds[left.production.lhs] ?: emptySet()) {
            add(i, leftRow, leftRow, rightCol)
          }
        }
        // byRowAndSymbol[leftRow][left.production.lhs][rightCol][parsedProduction.production] += middle
        // // complete entry causes partial entry (and empties before and after that if needed)
        // for (i in seeds[left.production.lhs] ?: emptySet()) {
        //   add(i, leftRow, leftRow, rightCol)
        // }
      }
      is PartiallyParsedProduction -> {
        if (byRowAndCol[leftRow][middle][parsedProduction].add(rightCol)) {
          // TODO: stop if nothing new added (for support of non-expanding productions)
          if (parsedProduction.nextSymbol in nullable) {
            add(parsedProduction, leftRow, rightCol, rightCol)
          }
        }
        // byRowAndCol[leftRow][middle][parsedProduction] += rightCol
        // if (parsedProduction.nextSymbol in nullable) {
        //   add(parsedProduction, leftRow, rightCol, rightCol)
        // }
      }
    }
  }
}

object Parser {
  // Assumes tokens have already been added
  fun parse(table: PartialParseTable): Unit {
    // TODO: proative add on existing entries

    for ((leftRowIndex, leftRowMap) in ReverseNavigableIterator(table.byRowAndCol.map)) {
      for ((leftColIndex, leftEntry) in NavigableIterator(leftRowMap.map)) {
        for ((leftSymbol, _) in NavigableIterator(leftEntry.map)) {
          val rightSymbol = leftSymbol.nextSymbol
          for ((rightColIndex, _) in NavigableIterator(table.byRowAndSymbol[leftColIndex][rightSymbol].map)) {
            // NOTE: use a proactive add instead of a reactive add
            table.add(leftSymbol, leftRowIndex, leftColIndex, rightColIndex)
          }
        }
      }
    }
  }
}
