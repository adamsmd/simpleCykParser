/** TODO. */

package org.michaeldadams.simpleCykParser.util

data class EqRegex(val pattern: String, val options: Set<RegexOption>) {
  val regex: Regex = Regex(pattern, options)

  constructor(pattern: String, option: RegexOption) : this(pattern, setOf(option))
  constructor(pattern: String) : this(pattern, emptySet())
}

fun Regex.toEqRegex(): EqRegex = EqRegex(this.pattern, this.options)
