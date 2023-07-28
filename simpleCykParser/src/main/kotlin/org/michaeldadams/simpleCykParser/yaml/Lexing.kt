/** Functions for converting Yaml to a grammar. */

package org.michaeldadams.simpleCykParser.yaml

import org.michaeldadams.simpleCykParser.lexing.Token
import kotlin.ranges.IntRange
import kotlin.text.MatchGroup

// ================================================================== //
// Token
// ================================================================== //

/**
 * Convert an [IntRange] object to its YAML representation.
 *
 * @receiver the object to convert to YAML
 * @return the YAML resulting from converting the object
 */
fun IntRange.toYamlString(): String = "[${start}, ${endInclusive}]"

/**
 * Convert a [MatchGroup] object to its YAML representation.
 *
 * @receiver the object to convert to YAML
 * @return the YAML resulting from converting the object
 */
fun MatchGroup?.toYamlString(): String =
  if (this == null) "null" else "${value.toYamlString()}: ${range.toYamlString()}"

/**
 * Convert a [Token] object to its YAML representation.
 *
 * @receiver the object to convert to YAML
 * @return the YAML resulting from converting the object
 */
fun Token.toYamlString(): String =
  "${terminal.name.toYamlString()}: [${groups.map { it.toYamlString() }.joinToString()}]"
