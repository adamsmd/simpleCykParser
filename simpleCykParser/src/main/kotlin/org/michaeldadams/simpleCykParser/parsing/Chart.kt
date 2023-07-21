/** TODO. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.util.AutoMap
import org.michaeldadams.simpleCykParser.util.TotalMap

// TODO: when to do "this."

/**
 * TODO.
 *
 * @property parseRules TODO
 */
@Suppress("TYPE_ALIAS")
class Chart(val parseRules: ParseRules) {
  /** Mutable backing field for [symbols]. */
  @Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
  private val _symbols: TotalMap<Int, TotalMap<Int, MutableSet<Symbol>>> =
    AutoMap { AutoMap { mutableSetOf() } }

  /**
   * The symbols in the chart.
   *
   * Can be added to using the [add] functions.
   *
   * Indexed (in order) by:
   * - start position (inclusive) and
   * - end position (exclusive).
   *
   * The final value is the set of symbols at that start and end position.
   */
  val symbols: TotalMap<Int, TotalMap<Int, Set<Symbol>>> = _symbols

  /** Mutable backing field for [items]. */
  @Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
  private val _items: TotalMap<Int, TotalMap<Int, TotalMap<Item, MutableSet<Int?>>>> =
    AutoMap { AutoMap { AutoMap { mutableSetOf() } } }

  // TODO: rename to splits?

  /**
   * The items in the chart.
   *
   * Can be added to using the [add] functions.
   *
   * Indexed (in order) by:
   * - start position (inclusive),
   * - end position (exclusive) and
   * - item.
   *
   * The final value is the set of positions where the TODO.
   */
  val items: TotalMap<Int, TotalMap<Int, TotalMap<Item, Set<Int?>>>> = _items

  /** Mutable backing field for [symbolEnds]. */
  @Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
  private val _symbolEnds: TotalMap<Int, TotalMap<Symbol, MutableSet<Int>>> =
    AutoMap { AutoMap { mutableSetOf() } }

  /**
   * Positions where a symbol ends given a start.
   *
   * Index (in order) by:
   * - start position (inclusive) and
   * - symbol.
   *
   * The final value is the set of positions where the given symbol TODO.
   *
   * TODO: looks along a row for a given right child
   * TODO: essentially symbols but indexed in a different order
   */
  val symbolEnds: TotalMap<Int, TotalMap<Symbol, Set<Int>>> = _symbolEnds

  /** Mutable backing field for [itemStarts]. */
  @Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
  private val _itemStarts: TotalMap<Int, TotalMap<Symbol, TotalMap<Int, MutableSet<Item>>>> =
    AutoMap { AutoMap { AutoMap { mutableSetOf() } } }

  /**
   * TODO.
   *
   * end -> nextSymbol -> start -> nextItem
   * TODO: looks up a column for a left child
   */
  val itemStarts: TotalMap<Int, TotalMap<Symbol, TotalMap<Int, Set<Item>>>> = _itemStarts

  // TODO: Splitting position for a start, end and PartialProduction. Null if
  // PartialProduction is present but has no splitting position (e.g., due to
  // empty or just being asserted).

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
      for ((itemStart, itemSet) in itemStarts[start][symbol].toMap()) {
        for (nextItem in itemSet.toSet()) {
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
   * @param item TODO
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
        for (nextEnd in symbolEnds[end][consumedSymbol].toSet()) {
          add(start, nextEnd, nextItem, end)
        }
      }
    }
  }

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
   */
  fun addEpsilonItems(): Unit {
    // TODO: symbolEnds.keys and itemStarts.keys?
    for (start in items.keys + symbols.keys) {
      for ((lhs, rhsSet) in parseRules.productionMap) {
        for (rhs in rhsSet) {
          add(start, start, Item(lhs, rhs, 0), null)
        }
      }
    }
  }
}

// (implicit start and end)
// symbol -> items
// item -> splits
// split -> left or right child

// get parents as left child (symbol)
// get parents as right child (item)

// get left
// get right
// get children
// get parses at
