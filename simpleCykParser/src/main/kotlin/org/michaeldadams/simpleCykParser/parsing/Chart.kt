package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.collections.AutoMap
import org.michaeldadams.simpleCykParser.collections.QueueSet
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol

// nt -> prod | prod | prod
// prod -> prod/2 prod.2
// prod/2 -> prod/1 prod.1
// prod/1 -> prod/0 prod.0
// prod/0 -> epsilon

// fun Production.toCompletePartialProduction
// fun Production.toInitialPartialProduction
// toPartial(0)
// toPartial(1)
// toPartial(-1)

// TODO: when to do "this."

data class PartialProduction(val production: Production, val consumed: Int) {
  init {
    require(consumed >= 0)
    require(consumed <= production.rhs.size)
  }
  val lastConsumed: Symbol get() = production.rhs[consumed]
  val isComplete: Boolean get() = consumed == production.rhs.size
  fun consume(): PartialProduction? =
    if (this.isComplete) null else PartialProduction(production, consumed + 1)
}

// TODO: lastPartial?

fun Production.toPartial(consumed: Int): PartialProduction {
  require(consumed >= 0)
  require(consumed <= this.rhs.size)
  return PartialProduction(this, consumed)
}

// TODO: move to Chart.kt
data class Chart(val parseRules: ProcessedParseRules, val size: Int) {
  // get left
  // get right
  // get children
  // get parses at
  // fromTokens
  // fromSymbols

  val symbols = Symbols()
  inner class Symbols {
    // Symbols for a start and end.
    private val keys: AutoMap<Int, AutoMap<Int, QueueSet<Symbol>>> =
      AutoMap { AutoMap { QueueSet() } }

    // Productions for a start, end, and symbol.  Null if "Symbol" is present but has no production.
    private val entries: AutoMap<Int, AutoMap<Int, AutoMap<Symbol, QueueSet<Production?>>>> =
      AutoMap { AutoMap { AutoMap { QueueSet() } } }

    // End for a start and symbol.
    private val ends: AutoMap<Int, AutoMap<Symbol, QueueSet<Int>>> =
      AutoMap { AutoMap { QueueSet() } }

    // Used to find parses
    operator fun get(start: Int, end: Int): Set<Symbol> = keys[start][end]

    // Used to get productions for parses
    operator fun get(start: Int, end: Int, symbol: Symbol): Set<Production?> = entries[start][end][symbol]

    // Used to get 'rightEnd'
    // TODO: production.toCompletePartialProduction
    operator fun get(start: Int, symbol: Symbol): Set<Int> = ends[start][symbol]

    // TODO: document
    operator fun plusAssign(entry: Pair<Pair<Int, Int>, Pair<Symbol, Production?>>): Unit {
      val (start, end) = entry.first
      val (symbol, production) = entry.second
      // val ((start, end), (symbol, production)) = entry // TODO: in argument?
      // NOTE: just an assertion and does not show how built
      // TODO: add always adds ProductionEntry (via initialUses)
      // chart.symbols += Pair(start, end) to Pair(symbol, production)
      if (production !in entries[start][end][symbol]) {
        keys[start][end] += symbol
        entries[start][end][symbol] += production
        ends[start][symbol] += end
        // If not in map, then has no initial uses
        for (newProduction in parseRules.initialUses.getOrDefault(symbol, emptySet())) {
          // NOTE: Addition goes up not down (we don't have info for down)
          productions += Pair(start, end) to Pair(newProduction.toPartial(1), start)
        }
      }
    }
  }

  val productions = Productions()
  inner class Productions {
    // PartialProductions for a start and end.
    private val keys: AutoMap<Int, AutoMap<Int, QueueSet<PartialProduction>>> =
      AutoMap { AutoMap { QueueSet() } }

    // Splitting position for a start, end and PartialProduction.  Null if PartialProduction is present but has no splitting position.
    private val entries: AutoMap<Int, AutoMap<Int, AutoMap<PartialProduction, QueueSet<Int?>>>> =
      AutoMap { AutoMap { AutoMap { QueueSet() } } }

    // End for a start and PartialProduction.
    private val ends: AutoMap<Int, AutoMap<PartialProduction, QueueSet<Int>>> =
      AutoMap { AutoMap { QueueSet() } }

    // Used to get 'leftChild'
    operator fun get(start: Int, end: Int): QueueSet<PartialProduction> = keys[start][end]

    // Used to get division between children
    operator fun get(start: Int, end: Int, partial: PartialProduction): QueueSet<Int?> = entries[start][end][partial]

    // Not used
    operator fun get(start: Int, partial: PartialProduction): QueueSet<Int> = ends[start][partial]

    // TODO: document
    operator fun plusAssign(entry: Pair<Pair<Int, Int>, Pair<PartialProduction, Int?>>): Unit {
      val (start, end) = entry.first
      val (partial, previous) = entry.second // TODO: in argument?
      // TODO: add always adds Symbol of the partialProd is complete
      if (previous !in entries[start][end][partial]) {
        keys[start][end] += partial
        entries[start][end][partial] += previous
        ends[start][partial] += end
        if (partial.isComplete) {
          // NOTE: Addition goes up not down (we don't have info for down)
          symbols += Pair(start, end) to Pair(partial.production.lhs, partial.production)
        }
      }
    }
  }

  init {
    for (position in 0..size) {
      for (production in parseRules.nullable) {
        for (consumed in 0..production.rhs.size) {
          productions += Pair(position, position) to Pair(production.toPartial(consumed), position)
        }
      }
    }
  }
}
