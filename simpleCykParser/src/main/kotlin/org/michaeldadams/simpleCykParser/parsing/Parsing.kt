package org.michaeldadams.simpleCykParser.parsing

// toPartial: Productions are unchanged, Symbols get their initial use Productions

// TODO: parseFromTokens
// TODO: logging of parse sequence
fun parse(chart: Chart): Unit {
  println("CHART(${chart.size})")
  for (leftStart in chart.size downTo 0) {
    for (leftEnd in leftStart..chart.size) {
      val rightStart = leftEnd
      // gets new elements if rightChild is nulled
      println("${leftStart}..${leftEnd}")
      if (chart.productions[leftStart].contains(leftEnd)) { // TODO: better way to prevent fill-in
        for (leftChild in chart.productions[leftStart][leftEnd].keysQueue) {
          println("  ${leftChild}")
          leftChild.consume()?.let { (nextPartial, consumed) ->
            // gets new elements if leftChild is nulled
            println("    $nextPartial")
            for (rightEnd in chart.symbolEnds[rightStart][consumed]) {
              chart.addProduction(Pair(leftStart, rightEnd) to Pair(nextPartial, leftEnd))
            }
          }
        }
      }
    }
  }
}
