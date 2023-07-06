package org.michaeldadams.simpleCykParser.parsing

// toPartial: Productions are unchanged, Symbols get their initial use Productions

// TODO: parseFromTokens

fun parse(chart: Chart): Unit {
  for (leftEnd in chart.size..0) {
    val rightStart = leftEnd
    for (leftStart in 0..leftEnd) {
      // gets new elements if rightChild is nulled
      for (leftChild in chart.productions[leftStart, leftEnd]) {
        leftChild.consume()?.let { nextPartial ->
          // gets new elements if leftChild is nulled
          for (rightEnd in chart.symbols[rightStart, nextPartial.lastConsumed]) {
            chart.productions += Pair(leftStart, rightEnd) to Pair(nextPartial, leftEnd)
          }
        }
      }
    }
  }
}
