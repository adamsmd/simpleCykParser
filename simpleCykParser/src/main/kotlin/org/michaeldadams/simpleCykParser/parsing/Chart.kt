/** The [Chart] class. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.util.AutoMap
import org.michaeldadams.simpleCykParser.util.TotalMap

/**
 * TODO.
 *
 * All start positions are inclusive and all end positions are exclusive.
 */
@Suppress("TYPE_ALIAS")
class Chart {
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
   * - start position and
   * - end position.
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
   * - start position,
   * - end position and
   * - item.
   *
   * The final value is the set of start positions for the most recently
   * consumed symbol in the item or `null` if the item has not consumed anything
   * yet.
   */
  val items: TotalMap<Int, TotalMap<Int, TotalMap<Item, Set<Int?>>>> = _items

  /** Mutable backing field for [symbolEnds]. */
  @Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
  private val _symbolEnds: TotalMap<Int, TotalMap<Symbol, MutableSet<Int>>> =
    AutoMap { AutoMap { mutableSetOf() } }

  /**
   * Positions where a symbol ends given a start.
   *
   * Indexed (in order) by:
   * - start position and
   * - symbol.
   *
   * The final value is the set of end positions for the symbol at the given
   * start position.
   *
   * This is used by [add] to find the positions of symbols to be consumed by an
   * item.
   *
   * This contains the same information as [symbols] but indexed in a different
   * order.
   */
  val symbolEnds: TotalMap<Int, TotalMap<Symbol, Set<Int>>> = _symbolEnds

  /** Mutable backing field for [nextItems]. */
  @Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
  private val _nextItems: TotalMap<Int, TotalMap<Symbol, TotalMap<Int, MutableSet<Item>>>> =
    AutoMap { AutoMap { AutoMap { mutableSetOf() } } }

  /**
   * Items that result from consuming a symbol.
   *
   * Indexed (in order) by:
   * - end position of the item,
   * - symbol to be consumed by an item, and
   * - start position of the item.
   *
   * The final value is the new item after the symbol has been consumed by an
   * item at the start and end positions.
   *
   * Used by [add] to find items to consume a symbol.
   */
  val nextItems: TotalMap<Int, TotalMap<Symbol, TotalMap<Int, Set<Item>>>> = _nextItems

  /**
   * Add an entry to [symbols] and update the chart as needed.
   *
   * @receiver the chart to which to add the entry
   * @param start the start position of the symbol to be added
   * @param end the end position of the symbol to be added
   * @param symbol the symbol to be added
   */
  fun add(start: Int, end: Int, symbol: Symbol): Unit {
    // Add the entry and do the following if it was not previously present
    if (_symbols[start][end].add(symbol)) {
      _symbolEnds[start][symbol] += end
      // Find item entries that can consume the symbol and add entries for the
      // item after consuming the symbol
      for ((nextItemStart, nextItemSet) in nextItems[start][symbol]) {
        for (nextItem in nextItemSet.toSet()) {
          add(nextItemStart, end, nextItem, start)
        }
      }
    }
  }

  /**
   * Add an entry to [items] and update the chart as needed.
   *
   * @receiver the chart to which to add the entry
   * @param start the start position of the item to be added
   * @param end the end position of the item to be added
   * @param item the item to be added
   * @param split the start position of the most recently consumed symbol or
   *   `null` if the item has not consumed anything yet
   */
  fun add(start: Int, end: Int, item: Item, split: Int?): Unit {
    // Add the entry and do the following if it was not previously present
    if (_items[start][end][item].add(split)) {
      // Check what symbol would be consumed next
      val consumed = item.consume()
      if (consumed == null) {
        // The item is already complete and cannot consume more
        // Add the symbol in the left-hand side of the item
        this.add(start, end, item.lhs)
      } else {
        // The item is not yet complete and can consume more
        val (consumedSymbol, nextItem) = consumed
        _nextItems[end][consumedSymbol][start] += nextItem
        // Find symbol entries that the item can consume and entries for the
        // item after consuming the symbol
        for (symbolEnd in symbolEnds[end][consumedSymbol]) {
          add(start, symbolEnd, nextItem, end)
        }
      }
    }
  }

  /**
   * Add a sequence of symbols to the chart.
   *
   * The first symbol is added at position [start], [start] + 1.
   * The next symbol is added at position [start] + 1, [start] + 1.
   * And so on.
   *
   * @receiver the chart to which to add the symbols
   * @param symbols the symbols to add to the start
   * @param start the start position at which to place the first symbol
   */
  // TODO: Add ItemList
  fun add(symbols: Iterable<Symbol>, start: Int = 0): Unit =
    symbols.forEachIndexed { index, symbol -> this.add(start + index, start + index + 1, symbol) }
}

// TODO:
// (implicit start and end)
// symbol -> items
// fun Chart.getItems(start: Int, end: Int, symbol: Symbol): Set<Item> = TODO()
// item -> splits: chart.
// split -> left or right child
// fun Chart.getLeft(start: Int, end: Int, item: Item): Item = TODO()
// fun Chart.getRight(start: Int, end: Int, item: Item): Symbol = TODO()

// get parents as left child (symbol)
// get parents as right child (item)

// get left
// get right
// get children
// get parses at
