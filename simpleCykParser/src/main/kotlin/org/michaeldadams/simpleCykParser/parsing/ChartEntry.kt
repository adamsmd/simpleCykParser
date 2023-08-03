/** The [ChartEntry] class and related code. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.util.Generated as Gen

/** TODO. */
sealed interface ChartEntry

/**
 * TODO.
 *
 * @property item TODO
 * @property split TODO
 */
@Gen data class ItemEntry(val item: Item, val split: Int?) : ChartEntry

/**
 * TODO.
 *
 * @property symbol TODO
 */
@Gen data class SymbolEntry(val symbol: Symbol) : ChartEntry

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.unconsumedItemEntries(): Set<ItemEntry> =
  productionMap.flatMap { (lhs, rhsSet) ->
    rhsSet.map { rhs ->
      ItemEntry(Item(lhs, rhs, 0), null)
    }
  }.toSet()

/**
 * TODO: Add epsilon items (i.e., items that have not consumed anything) for
 * all positions for all productions.
 *
 * @receiver TODO: the chart to which to add epsilon items
 * @param start TODO
 * @param end TODO
 * @param chartEntry TODO
 */
fun Chart.add(start: Int, end: Int, chartEntry: ChartEntry): Unit {
  when (chartEntry) {
    is SymbolEntry -> add(start, end, chartEntry.symbol)
    is ItemEntry -> add(start, end, chartEntry.item, chartEntry.split)
  }
}

/**
 * TODO: Add epsilon items (i.e., items that have not consumed anything) for
 * all positions for all productions.
 *
 * @receiver TODO: the chart to which to add epsilon items
 * @param parseRules TODO
 */
fun Chart.addUnconsumedItemEntries(parseRules: ParseRules): Unit {
  val entries = parseRules.unconsumedItemEntries()
  val positions =
    symbols.keys + items.keys + symbols.flatMap { it.value.keys } + items.flatMap { it.value.keys }
  for (position in positions.toSet()) {
    for (entry in entries) {
      add(position, position, entry)
    }
  }
}
