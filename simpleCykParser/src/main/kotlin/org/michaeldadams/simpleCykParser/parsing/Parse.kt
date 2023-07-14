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
  val size = chart.entries.keys.max() + 1 // TODO: alternative to "size"
  for (leftStart in size downTo 0) {
    // Note that rightStart == leftEnd
    for (leftEnd in leftStart..size) {
      // gets new elements if rightChild is nulled
      // TODO: better way to prevent fill-in
      if (chart.entries[leftStart].contains(leftEnd)) {
        // TODO: override QueueMap.entries
        // for ((leftSymbol, leftRhss) in chart.entries[leftStart][leftEnd].entries) {
        for (leftSymbol in chart.entries[leftStart][leftEnd].keys) {
          for (leftRhs in chart.entries[leftStart][leftEnd][leftSymbol].keys) {
            if (leftRhs != null) {
              if (leftSymbol !is Nonterminal) {
                throw AssertionError(TODO() as String) // TODO: try as assertion
              } else {
                for (consumed in chart.entries[leftStart][leftEnd][leftSymbol][leftRhs].keys) {
                  if (consumed < leftRhs.parts.size) {
                    // TODO: ?? gets new elements if leftChild is nulled
                    // TODO: toNext() trick
                    val newConsumed = consumed + 1
                    val consumedSymbol = leftRhs.parts[consumed].second
                    for (rightEnd in chart.symbolEnds[leftEnd][consumedSymbol]) {
                      chart.add(leftStart, rightEnd, leftSymbol, leftRhs, newConsumed, leftEnd)
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
}
