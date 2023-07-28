/** Functions for converting Yaml to a grammar. */

package org.michaeldadams.simpleCykParser.yaml

import org.michaeldadams.simpleCykParser.lexing.Token
import kotlin.ranges.IntRange
import kotlin.text.MatchGroup

// ================================================================== //
// Token
// ================================================================== //

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun IntRange.toYamlString(): String = "[${start}, ${endInclusive}]"

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun MatchGroup?.toYamlString(): String =
  if (this == null) "null" else "${value.toYamlString()}: ${range.toYamlString()}"

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Token.toYamlString(): String =
  "${terminal.name.toYamlString()}: [${groups.map { it.toYamlString() }.joinToString()}]"

//  "A": ["xxx": [12,15], "xx": [13,14]]
//  "A": ["xxx": [12,15], [], "xx": [13,14]]
//  "A": [["xxx", 12,15], "xx": [13,14]]
