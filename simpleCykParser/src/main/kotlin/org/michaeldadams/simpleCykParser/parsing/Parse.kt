/** A simple implementation of the CYK parsing algorithm. */

package org.michaeldadams.simpleCykParser.parsing

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
      if (chart.productions[leftStart].contains(leftEnd)) {
        for (leftChild in chart.productions[leftStart][leftEnd].keys) {
          leftChild.toNext()?.let { (nextPartial, consumed) ->
            // gets new elements if leftChild is nulled
            for (rightEnd in chart.symbolEnds[rightStart][consumed]) {
              chart.addProduction(leftStart, rightEnd, nextPartial, leftEnd)
            }
          }
        }
      }
    }
  }
}
