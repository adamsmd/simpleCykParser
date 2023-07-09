/** A simple implementation of the CYK parsing algorithm. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Symbol
// toPartial: Productions are unchanged, Symbols get their initial use Productions

// TODO: parseFromTokens

/**
 * TODO.
 *
 * @param chart
 */
fun parse(chart: Chart): Unit {
  // TODO: document
  for (leftStart in chart.size downTo 0) {
    for (leftEnd in leftStart..chart.size) {
      val rightStart = leftEnd
      // gets new elements if rightChild is nulled
      // TODO: better way to prevent fill-in
      if (chart.entries[leftStart].contains(leftEnd)) {
        // TODO: override QueueMap.entries
        // for ((leftSymbol, leftProductions) in chart.entries[leftStart][leftEnd].entries) {
        for (leftSymbol in chart.entries[leftStart][leftEnd].keys) {
          for (leftProduction in chart.entries[leftStart][leftEnd][leftSymbol].keys) {
            if (leftProduction != null) {
              for (consumed in chart.entries[leftStart][leftEnd][leftSymbol][leftProduction].keys) {
                if (consumed < leftProduction.rhs.size) {
                  // TODO: ?? gets new elements if leftChild is nulled
                  val consumedSymbol: Symbol = leftProduction.rhs[consumed].second // TODO: toNext() trick
                  for (rightEnd in chart.symbolEnds[rightStart][consumedSymbol]) {
                    // chart.addProduction(leftStart, rightEnd, nextPartial, rightStart)
                    chart.addProduction(leftStart, rightEnd, leftProduction, consumed + 1, rightStart)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
