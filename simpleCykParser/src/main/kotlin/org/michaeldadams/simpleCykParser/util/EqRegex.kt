/** The [EqRegex] class and helpers. */

package org.michaeldadams.simpleCykParser.util

import org.michaeldadams.simpleCykParser.util.Generated as Gen

/**
 * Regular expression that supports equality checking between regular
 * expressions.
 *
 * Kotlin's [Regex] does not implement structural equality.  Since
 * [TerminalRule] contains a regular expression, this would make [TerminalRule]
 * and all the types that contain it not implement structural equality. This is
 * a problem for testing, so we use this type as an alternative to [Regex].
 *
 * @property pattern the pattern string of this regular expression
 * @property options the set of options that were used to create this regular
 *   expression
 */
data class EqRegex(@get:Gen val pattern: String, @get:Gen val options: Set<RegexOption>) {
  /** The regular expression represented by this [EqRegex]. */
  val regex: Regex = Regex(pattern, options)
}

/**
 * Convert a [Regex] to an [EqRegex].
 *
 * @receiver the [Regex] to convert to an [EqRegex]
 * @return the [EqRegex] equivalent of [Regex]
 */
fun Regex.toEqRegex(): EqRegex = EqRegex(this.pattern, this.options)
