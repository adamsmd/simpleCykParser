/** TODO. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.util.QueueMap
import org.michaeldadams.simpleCykParser.util.QueueSet
import org.michaeldadams.simpleCykParser.util.queueMap

// TODO: when to do "this."

typealias ChartEntryMap<T> =
  QueueMap<Int, QueueMap<Int, QueueMap<Symbol, QueueMap<Production?, QueueMap<Int, T>>>>>

// @Suppress("TYPE_ALIAS")
/**
 * TODO.
 *
 * @property parser TODO
 * @property size TODO
 */
class Chart(val parser: Parser, val size: Int) {
  // TODO: size is inclusive
  // get left
  // get right
  // get children
  // get parses at
  // fromTokens
  // fromSymbols
  // Used to get division between children

  // TODO: typealias for _entries
  /** Mutable backing field for [entries]. */
  private val _entries: ChartEntryMap<QueueSet<Int?>> =
    queueMap { queueMap { queueMap { queueMap { queueMap { QueueSet() } } } } }

  /** TODO.
   * Start
   * End
   * Symbol
   * Production (null means no justification)
   * Consumed
   * Split (null if consumed == 0 or no justification(TODO: remove))
   */
  val entries: ChartEntryMap<Set<Int?>> = _entries

  // End for a start and symbol.
  // Used to get 'rightEnd'
  /** Mutable backing field for [symbolEnds]. */
  private val _symbolEnds: QueueMap<Int, QueueMap<Symbol, QueueSet<Int>>> =
    queueMap { queueMap { QueueSet() } }

  /** TODO. */
  val symbolEnds: QueueMap<Int, QueueMap<Symbol, Set<Int>>> = _symbolEnds

  // TODO: Splitting position for a start, end and PartialProduction.  Null if PartialProduction is present but has no splitting position (e.g., due to empty or just being asserted).

  init {
    for (position in 0..size) {
      for (production in parser.nullable) {
        for (consumed in 0..production.rhs.size) {
          val split = if (consumed == 0) null else position
          this.addProduction(position, position, production, consumed, split)
        }
      }
    }
  }

  // TODO: document
  constructor(parser: Parser, vararg terminals: Terminal) : this(parser, terminals.size) {
    for ((start, terminal) in terminals.withIndex()) {
      this.addSymbol(start, start + 1, terminal)
    }
  }

  constructor(parser: Parser, vararg terminals: String) :
    this(parser, *terminals.map(::Terminal).toTypedArray())

  /**
   * TODO.
   *
   * @param start TODO
   * @param end TODO
   * @param symbol TODO
   * @param production TODO
   */
  fun addSymbol(start: Int, end: Int, symbol: Symbol): Unit {
    _entries[start][end][symbol][null]
    _addSymbol(start, end, symbol)
  }
  private fun _addSymbol(start: Int, end: Int, symbol: Symbol): Unit {
    // NOTE: just an assertion and does not show how built
    // TODO: add always adds ProductionEntry (via initialUses)
    require(_entries[start][end][symbol].isNotEmpty()) { "TODO" }
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
   * @param start TODO
   * @param end TODO
   * @param partial TODO
   * @param split TODO
   */
  fun addProduction(start: Int, end: Int, production: Production, consumed: Int, split: Int?): Unit {
    require(consumed >= 0) { "TODO" }
    require(consumed <= production.rhs.size) { "TODO" }
    // TODO: add always adds Symbol of the partialProd is complete
    if (_entries[start][end][production.lhs][production][consumed].add(split)) {
      if (production.rhs.size == consumed) {
        // NOTE: Addition goes up not down (we don't have info for down)
        this._addSymbol(start, end, production.lhs)
      }
    }
  }

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
                  println("- [${start}, ${end}, ${symbol}, ${production.toYamlString()}, ${consumed}, ${split}]")
                }
              }
            }
          }
        }
      }
    }
  }
}

/*

- [5, 6, A]
- [5, 6, A, X: [B, C: D]]
- [5, 6, A, X: [B, C: D], 1]
- [5, 6, A, X: [B, C: D], 5, 5]

*/
