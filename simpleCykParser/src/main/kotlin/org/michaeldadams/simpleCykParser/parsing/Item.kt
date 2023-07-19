/** TODO. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.ProductionMap
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.initialUses
import org.michaeldadams.simpleCykParser.grammar.nullable
import org.michaeldadams.simpleCykParser.grammar.nullablePrefixes

data class Item(val lhs: Nonterminal, val rhs: Rhs, val consumed: Int) {
  init {
    require(consumed >= 0) { "Consumed (${consumed}) must be non-negative" }
    require(consumed <= rhs.elements.size) {
      "Consumed (${consumed}) must not be greater than the number of elements (${rhs.elements.size})"
    }
  }

  // TODO: swap order of pair
  fun consume(): Pair<Symbol, Item>? =
    rhs.elements.getOrNull(consumed)?.let { Pair(it.symbol, Item(lhs, rhs, consumed + 1)) }

  fun isComplete(): Boolean = consumed == rhs.elements.size
}

fun ParseRules.nullablePartials(): Set<Item> =
  this.nullablePrefixes().flatMap { (lhs, lhsMap) ->
    lhsMap.flatMap { (rhs, prefix) ->
      (0..prefix).map { consumed -> Item(lhs, rhs, consumed) }
    }
  }.toSet()

fun ParseRules.initialItems(): Map<Symbol?, Set<Item>> =
  this.initialUses().mapValues { entry ->
    entry.value.flatMap { (lhs, rhsSet: Set<Rhs>) ->
      rhsSet.map { rhs -> Item(lhs, rhs, if (entry.key == null) 0 else 1) }
    }.toSet()
  }