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
 * A map for chart entries.
 *
 * In order, indexed by:
 * - start position (inclusive),
 * - end position (exclusive),
 * - symbol at that start an end position,
 * - right-hand side (if any) that produced that symbol,
 * - consumed ho (map non-empty only if the right-hand side is non-null)
 *
 * symbol (no production)
 * production (with consumed)
 *
 * @param T TODO
 */
// TODO: rename to position map
typealias SymbolMap<T> = QueueMap<Int, QueueMap<Int, T>>
// TODO: rename to partial map
typealias ItemMap<T> = QueueMap<Int, QueueMap<Int, QueueMap<Item, T>>>

/**
 * TODO.
 *
 * @param T TODO
 */
// TODO: rename
typealias SymbolEndsMap<T> = QueueMap<Int, QueueMap<Symbol, T>>

/**
 * TODO.
 *
 * @property parser TODO
 */
class Chart(val parser: Parser) {
  /** Mutable backing field for [symbols]. */
  @Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
  private val _symbols: SymbolMap<QueueSet<Symbol>> =
    queueMap { start ->
      queueMap<Int, QueueSet<Symbol>> { QueueSet() }.also { it[start].addAll(parser.nullable) }
    }

  val symbols: SymbolMap<Set<Symbol>> = _symbols

  /** Mutable backing field for [productions]. */
  @Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
  private val _items: ItemMap<QueueSet<Int?>> =
    queueMap { start ->
      queueMap<Int, QueueMap<Item, QueueSet<Int?>>> {
        queueMap { QueueSet() }
      }.also { qm ->
        // println("init $start")
        for (item in parser.nullablePartials) {
          // println("  $item")
          for (consumed in 0..item.consumed) {
            // println("    $consumed")
            // TODO: start instead of null?
            qm[start][item].add(if (consumed == 0) null else start)
          }
        }
        // println()
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
  val items: ItemMap<Set<Int?>> = _items

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
    // NOTE: just an assertion and does not show how built
    // TODO: add always adds ProductionEntry (via initialUses)
    _symbols[start][end] += symbol
    _symbolEnds[start][symbol] += end
    // println("+ $start $end $symbol")
    // for (item in _items[start][end].keys) {
    //   println("  + $item")
    // }
    // If not in map, then has no initial uses
    for (item in parser.initialItems.getOrDefault(symbol, emptySet())) {
      // println("  - $item")
      // NOTE: Addition goes up not down (we don't have info for down)
      this.add(start, end, item, start)
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
  fun add(start: Int, end: Int, item: Item, split: Int?): Unit {
    // require(lhs in parser.parseRules)
    // require(rhs in parser.parseRules[lhs])
    // TODO: add always adds Symbol of the partialProd is complete
    if (_items[start][end][item].add(split) && item.isComplete()) {
      // NOTE: Addition goes up not down (we don't have info for down)
      this.add(start, end, item.lhs)
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
  println("symbols:")
  for ((start, startValue) in this.symbols.entries) {
    println("  ################################")
    println("  # start: ${start}")
    for ((end, endValue) in startValue.entries) {
      println("  # start: ${start} end: ${end}")
      for (symbol in endValue) {
        println("  - [${start}, ${end}, ${symbol.toYamlString()}]")
      }
      println()
    }
  }

  println("productions:")
  for ((start, startValue) in this.items.entries) {
    println("  ################################")
    println("  # start: ${start}")
    for ((end, endValue) in startValue.entries) {
      println("  # start: ${start} end: ${end}")
      for ((item, itemValue) in endValue.entries) {
        for (split in itemValue) {
          if (split == null) {
            @Suppress("MaxLineLength", "ktlint:argument-list-wrapping", "ktlint:max-line-length")
            println("  - [${start}, ${end}, ${item.lhs.name}, ${item.rhs.toYamlString()}, ${item.consumed}]")
          } else {
            @Suppress("MaxLineLength", "ktlint:argument-list-wrapping", "ktlint:max-line-length")
            println("  - [${start}, ${end}, ${item.lhs.name}, ${item.rhs.toYamlString()}, ${item.consumed}, ${split}]")
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
