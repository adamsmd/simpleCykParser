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
  private val _symbols: SymbolMap<QueueSet<Symbol>> = queueMap { queueMap { QueueSet() } }

  val symbols: SymbolMap<Set<Symbol>> = _symbols

  /** Mutable backing field for [productions]. */
  @Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
  private val _items: ItemMap<QueueSet<Int?>> = queueMap { queueMap { queueMap { QueueSet() } } }

  /** TODO.
   * Start (inclusive)
   * End (exclusive)
   * Symbol
   * Production (null means no justification)
   * Consumed
   * Split (null if consumed == 0 or no justification(TODO: remove))
   */
  val items: ItemMap<Set<Int?>> = _items

  fun addEpsilonItems(): Unit {
    for (start in items.keys + symbols.keys) {
      for ((lhs, rhsSet) in parser.parseRules.productionMap) {
        for (rhs in rhsSet) {
          add(start, start, Item(lhs, rhs, 0), null)
        }
      }
    }
  }

  // TODO: End for a start and symbol.
  // TODO: Used to get 'rightEnd'

  /** Mutable backing field for [symbolEnds]. */
  @Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
  private val _symbolEnds: SymbolEndsMap<QueueSet<Int>> = queueMap { queueMap { QueueSet() } }

  /** TODO.
   * Start
   * Symbol
   * End
   */
  val symbolEnds: SymbolEndsMap<Set<Int>> = _symbolEnds

  // end -> nextSymbol -> start -> nextItem
  val _itemStarts: QueueMap<Int, QueueMap<Symbol, QueueMap<Int, QueueSet<Item>>>> =
    queueMap { queueMap { queueMap { QueueSet() } } }

  val itemStarts: QueueMap<Int, QueueMap<Symbol, QueueMap<Int, Set<Item>>>> = _itemStarts

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
    if (_symbols[start][end].add(symbol)) {
      _symbolEnds[start][symbol] += end
      for ((itemStart, itemSet) in itemStarts[start][symbol]) {
        for (nextItem in itemSet) {
          add(itemStart, end, nextItem, start)
        }
      }
    }
  }

  // TODO: find all "middle" and call them "split"
  // val nextItem[start][nextEnd/start][nextItem][end/end]
  // val nextItem[end][ConsumedSymbol] = Set<start>

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
    // require(split between start and end)
    // require(start <= end)
    // require(lhs in parser.parseRules)
    // require(rhs in parser.parseRules[lhs])
    if (_items[start][end][item].add(split)) {
      val consumed = item.consume()
      if (consumed == null) {
        // NOTE: Addition goes up not down (we don't have info for down)
        this.add(start, end, item.lhs)
      } else {
        val (consumedSymbol, nextItem) = consumed
        _itemStarts[end][consumedSymbol][start] += nextItem
        for (nextEnd in symbolEnds[end][consumedSymbol]) {
          add(start, nextEnd, nextItem, end)
        }
      }
    }
  }
}

// TODO: move to Yaml.kt? (Move Yaml.kt to util/ ? yaml/ ?)
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

  println("items:")
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
