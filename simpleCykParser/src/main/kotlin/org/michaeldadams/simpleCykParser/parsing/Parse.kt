/** A simple implementation of the CYK parsing algorithm. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Nonterminal

// TODO: fun parse(parser, tokens: List<String>)
// TODO: fun parse(parser, tokens: List<Nonterminal>)
// TODO: fun parse(chart, tokens: List<String>)
// TODO: fun parse(chart, tokens: List<Nonterminal>)

/**
 * TODO.
 *
 * @param chart
 */
@Suppress(
  "CognitiveComplexMethod",
  "NestedBlockDepth",
  "kotlin:S3776", // Cognitive Complexity of functions should not be too high
)
fun parse(chart: Chart): Unit {
  // TODO: start and end as parameters
  // TODO: document
  // TODO: .sorted().reverse()
  val size = chart.symbols.keys.max() + 1 // TODO: alternative to "size"
  for (leftStart in size downTo 0) {
    // Note that rightStart == leftEnd
    for (leftEnd in leftStart..size) {
      // gets new elements if rightChild is nulled
      // TODO: better way to prevent fill-in
      if (leftEnd in chart.items[leftStart]) {
        // TODO: override QueueMap.entries
        for (leftItem in chart.items[leftStart][leftEnd].keys) {
          // println("$leftStart $leftEnd $leftItem")
          leftItem.consume()?.let { (consumedSymbol, nextItem)->
            for (rightEnd in chart.symbolEnds[leftEnd][consumedSymbol]) {
              chart.add(leftStart, rightEnd, nextItem, leftEnd)
            }
            // TODO: ?? gets new elements if leftChild is nulled
          }
        }
      }
    }
  }
}
