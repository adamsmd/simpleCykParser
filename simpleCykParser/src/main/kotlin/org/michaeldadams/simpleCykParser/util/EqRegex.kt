/** TODO. */

package org.michaeldadams.simpleCykParser.util

/**
 * TODO.
 *
 * @property pattern TODO
 * @property options TODO
 */
data class EqRegex(val pattern: String, val options: Set<RegexOption>) {
  /** TODO. */
  val regex: Regex = Regex(pattern, options)

  /**
   * TODO.
   *
   * @param pattern TODO
   * @param options TODO
   */
  constructor(pattern: String, vararg options: RegexOption) : this(pattern, options.toSet())
}

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Regex.toEqRegex(): EqRegex = EqRegex(this.pattern, this.options)
