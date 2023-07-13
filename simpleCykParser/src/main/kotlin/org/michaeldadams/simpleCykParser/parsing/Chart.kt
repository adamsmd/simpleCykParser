/** TODO. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.toYamlString
import org.michaeldadams.simpleCykParser.util.QueueMap
import org.michaeldadams.simpleCykParser.util.QueueSet
import org.michaeldadams.simpleCykParser.util.queueMap

// TODO: when to do "this."

/**
 * TODO.
 * - start
 * - end
 * - symbol
 * - production
 * - consumed
 *
 * @param T TODO
 */
typealias ChartEntryMap<T> =
  QueueMap<Int, QueueMap<Int, QueueMap<Symbol, QueueMap<Production?, QueueMap<Int, T>>>>>

/**
 * TODO.
 *
 * @param T TODO
 */
typealias SymbolEndsMap<T> = QueueMap<Int, QueueMap<Symbol, T>>

/**
 * TODO.
 *
 * @property parser TODO
 */
class Chart(val parser: Parser) {
  // TODO: size is inclusive
  // get left
  // get right
  // get children
  // get parses at
  // fromTokens
  // fromSymbols
  // Used to get division between children
  // private val defaultEntries: Map<Production?, Set<Int>> = {
  //   null -> emptySet()
  //   production in parser.nullable -> production.rhs.size
  //   initialUses[
  // }

  /** Mutable backing field for [entries]. */
  @Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
  private val _entries: ChartEntryMap<QueueSet<Int?>> =
    queueMap { start ->
      queueMap<Int, QueueMap<Symbol, QueueMap<Production?, QueueMap<Int, QueueSet<Int?>>>>> {
        queueMap { queueMap { queueMap { QueueSet() } } }
      }.also { qm ->
        for ((production, consumedSet) in parser.partiallyNullable) {
          for (consumed in consumedSet) {
            qm[start][production.lhs][production][consumed].add(if (consumed == 0) null else start)
          }
        }
      }
    }

  /** TODO.
   * Start
   * End
   * Symbol
   * Production (null means no justification)
   * Consumed
   * Split (null if consumed == 0 or no justification(TODO: remove))
   */
  val entries: ChartEntryMap<Set<Int?>> = _entries

  // TODO: End for a start and symbol.
  // TODO: Used to get 'rightEnd'

  /** Mutable backing field for [symbolEnds]. */
  @Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
  private val _symbolEnds: SymbolEndsMap<QueueSet<Int>> =
    queueMap { start ->
      queueMap<Symbol, QueueSet<Int>> { QueueSet() }.also { qm ->
        parser.nullableSymbols.forEach { qm[it].add(start) }
      }
    }

  /** TODO.
   * Start
   * Symbol
   * End
   */
  val symbolEnds: SymbolEndsMap<Set<Int>> = _symbolEnds

  // TODO: Splitting position for a start, end and PartialProduction. Null if
  // PartialProduction is present but has no splitting position (e.g., due to
  // empty or just being asserted).

  // TODO: fun addSymbols and then remove the constructor
  fun addSymbols(symbols: List<Symbol>): Unit =
    symbols.forEachIndexed { start, symbol -> this.addSymbol(start, start + 1, symbol) }

  /**
   * TODO.
   *
   * @receiver TODO
   * @param start TODO
   * @param end TODO
   * @param symbol TODO
   */
  fun addSymbol(start: Int, end: Int, symbol: Symbol): Unit {
    _entries[start][end][symbol][null]
    addInitializedSymbol(start, end, symbol)
  }

  // TODO: better name

  /**
   * TODO.
   *
   * @receiver TODO
   * @param start TODO
   * @param end TODO
   * @param symbol TODO
   */
  private fun addInitializedSymbol(start: Int, end: Int, symbol: Symbol): Unit {
    // NOTE: just an assertion and does not show how built
    // TODO: add always adds ProductionEntry (via initialUses)
    assert(_entries[start][end][symbol].isNotEmpty()) {
      "Not initialized: ${start}:${end}:${symbol}"
    }
    _symbolEnds[start][symbol].add(end)
    // If not in map, then has no initial uses
    for (newProduction in parser.initialUses.getOrDefault(symbol, emptySet())) {
      // NOTE: Addition goes up not down (we don't have info for down)
      this.addProduction(start, end, newProduction, 1, start)
    }
  }

  // TODO: find all "middle" and call them "split"

  /**
   * TODO.
   *
   * @receiver TODO
   * @param start TODO
   * @param end TODO
   * @param production TODO
   * @param consumed TODO
   * @param split TODO
   */
  fun addProduction(start: Int, end: Int, production: Production, consumed: Int, split: Int?): Unit {
    require(consumed >= 0) { "Consumed must be non-negative but is ${consumed}" }
    require(consumed <= production.rhs.size) {
      "Consumed must be less than or equal to the rhs length (${production.rhs.size}) but is ${consumed}"
    }
    // require(production.symbol in parser.parseRules)
    // require(production in parser.parseRules[production.symbol])
    // TODO: add always adds Symbol of the partialProd is complete
    if (_entries[start][end][production.lhs][production][consumed].add(split) &&
      production.rhs.size == consumed
    ) {
      // NOTE: Addition goes up not down (we don't have info for down)
      this.addInitializedSymbol(start, end, production.lhs)
    }
  }

  // TODO: move to Yaml.kt? (Move Yaml.kt to util/ ? yaml/ ?)

  /**
   * TODO.
   *
   * @receiver TODO
   */
  fun printEntries(): Unit {
    for ((start, startValue) in this.entries.entries) {
      for ((end, endValue) in startValue.entries) {
        for ((symbol, symbolValue) in endValue.entries) {
          for ((production, productionValue) in symbolValue.entries) {
            if (production == null) {
              println("- [${start}, ${end}, ${symbol}]")
            } else {
              for ((consumed, consumedValue) in productionValue.entries) {
                for (split in consumedValue) {
                  if (split == null) {
                    @Suppress("MaxLineLength", "ktlint:argument-list-wrapping", "ktlint:max-line-length")
                    println("- [${start}, ${end}, ${symbol}, ${production.toYamlString()}, ${consumed}]")
                  } else {
                    @Suppress("MaxLineLength", "ktlint:argument-list-wrapping", "ktlint:max-line-length")
                    println("- [${start}, ${end}, ${symbol}, ${production.toYamlString()}, ${consumed}, ${split}]")
                  }
                }
              }
            }
          }
        }
        println()
      }
      println()
    }
  }
}

/*

- [5, 6, A]
- [5, 6, A, X: [B, C: D]]
- [5, 6, A, X: [B, C: D], 1]
- [5, 6, A, X: [B, C: D], 5, 5]

*/
