/** TODO. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.Symbol

data class Item(val lhs: Nonterminal, val rhs: Rhs, val consumed: Int) {
  init {
    require(consumed >= 0) { "Consumed (${consumed}) must be non-negative" }
    require(consumed <= rhs.elements.size) {
      "Consumed (${consumed}) must not be greater than the number of elements (${rhs.elements.size})"
    }
  }

  fun consume(): Pair<Symbol, Item>? =
    rhs.elements.getOrNull(consumed)?.let { Pair(it.symbol, Item(lhs, rhs, consumed + 1)) }
}
