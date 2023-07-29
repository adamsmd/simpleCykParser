/** The [Item] class. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.util.Generated as Gen

/**
 * A parsing item (i.e., a production annotated with how much of the right-hand
 * side has been consumed).
 *
 * @property lhs the left-hand side of the production for this item
 * @property rhs the right-hand side of the production for this item
 * @property consumed how many elements have been consumed of the right-hand
 *   side of the production of this item
 */
data class Item(@get:Gen val lhs: Nonterminal, @get:Gen val rhs: Rhs, @get:Gen val consumed: Int) {
  init {
    require(consumed >= 0) { "Consumed (${consumed}) must be non-negative" }
    require(consumed <= rhs.elements.size) {
      "Consumed (${consumed}) must be no greater than the number of elements (${rhs.elements.size})"
    }
  }

  /**
   * Advance the number of elements [consumed] by this item.
   *
   * @return `null` if all elements are already consumed and the item is
   *   complete; otherwise a pair of the symbol that was advanced over and the
   *   item after the symbol is consumed
   */
  fun consume(): Pair<Symbol, Item>? =
    rhs.elements.getOrNull(consumed)?.let { Pair(it.symbol, Item(lhs, rhs, consumed + 1)) }
}
