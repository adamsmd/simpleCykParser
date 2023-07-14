/** TODO. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.toYamlString
import org.michaeldadams.simpleCykParser.util.QueueMap
import org.michaeldadams.simpleCykParser.util.QueueSet
import org.michaeldadams.simpleCykParser.util.fromSetsMap
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
  QueueMap<Int, QueueMap<Int, QueueMap<Symbol, QueueMap<Rhs?, QueueMap<Int, T>>>>>

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
  /** Mutable backing field for [entries]. */
  @Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
  private val _entries: ChartEntryMap<QueueSet<Int?>> =
    queueMap { start ->
      queueMap<Int, QueueMap<Symbol, QueueMap<Rhs?, QueueMap<Int, QueueSet<Int?>>>>> {
        queueMap { queueMap { queueMap { QueueSet() } } }
      }.also { qm ->
        for ((lhs, rhsMap) in parser.partiallyNullable) {
          for ((rhs, consumedSet) in rhsMap) {
            for (consumed in consumedSet) {
              qm[start][lhs][rhs][consumed].add(if (consumed == 0) null else start)
            }
          }
        }
      }
    }

  /** TODO.
   * Start (inclusive)
   * End (exclusive)
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
        parser.nullable.forEach { qm[it].add(start) }
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

  /**
   * TODO.
   *
   * @receiver TODO
   * @param symbols TODO
   */
  fun add(symbols: Iterable<Symbol>): Unit =
    symbols.forEachIndexed { start, symbol -> this.add(start, start + 1, symbol) }

  /**
   * TODO.
   *
   * @receiver TODO
   * @param start TODO
   * @param end TODO
   * @param symbol TODO
   */
  fun add(start: Int, end: Int, symbol: Symbol): Unit {
    _entries[start][end][symbol][null]
    addImpl(start, end, symbol)
  }

  /**
   * TODO: rename to put?
   *
   * @receiver TODO
   * @param start TODO
   * @param end TODO
   * @param symbol TODO
   */
  private fun addImpl(start: Int, end: Int, symbol: Symbol): Unit {
    // NOTE: just an assertion and does not show how built
    // TODO: add always adds ProductionEntry (via initialUses)
    assert(_entries[start][end][symbol].isNotEmpty()) { "Uninitialized: ${start}:${end}:${symbol}" }
    _symbolEnds[start][symbol].add(end)
    // If not in map, then has no initial uses
    for ((lhs, rhs) in parser.initialUses.getOrDefault(symbol, emptyMap()).fromSetsMap()) {
      // NOTE: Addition goes up not down (we don't have info for down)
      this.add(start, end, lhs, rhs, 1, start)
    }
  }

  // TODO: find all "middle" and call them "split"

  /**
   * TODO.
   *
   * @receiver TODO
   * @param start TODO
   * @param end TODO
   * @param lhs TODO
   * @param rhs TODO
   * @param consumed TODO
   * @param split TODO
   */
  fun add(start: Int, end: Int, lhs: Nonterminal, rhs: Rhs, consumed: Int, split: Int?): Unit {
    require(consumed >= 0) { "Consumed must be non-negative but is ${consumed}" }
    require(consumed <= rhs.parts.size) {
      "Consumed must be less than or equal to the rhs length (${rhs.parts.size}) but is ${consumed}"
    }
    // require(lhs in parser.parseRules)
    // require(rhs in parser.parseRules[lhs])
    // TODO: add always adds Symbol of the partialProd is complete
    if (_entries[start][end][lhs][rhs][consumed].add(split) && rhs.parts.size == consumed) {
      // NOTE: Addition goes up not down (we don't have info for down)
      this.addImpl(start, end, lhs)
    }
  }

  // TODO: move to Yaml.kt? (Move Yaml.kt to util/ ? yaml/ ?)
}

/**
 * TODO.
 *
 * @receiver TODO
 */
fun Chart.printEntries(): Unit {
  for ((start, startValue) in this.entries.entries) {
    println("################################")
    println("# start: ${start}")
    for ((end, endValue) in startValue.entries) {
      println("# start: ${start} end: ${end}")
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
  }
}

/*

- [5, 6, A]
- [5, 6, A, X: [B, C: D]]
- [5, 6, A, X: [B, C: D], 1]
- [5, 6, A, X: [B, C: D], 5, 5]

*/

// get left
// get right
// get children
// get parses at
// Used to get division between children
