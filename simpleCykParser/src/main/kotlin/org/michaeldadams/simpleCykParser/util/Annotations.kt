/** Annotation classes. */

@file:Suppress(
  "FILE_NAME_MATCH_CLASS",
  "MatchingDeclarationName",
  "ktlint:standard:filename",
)

package org.michaeldadams.simpleCykParser.util

/**
 * Annotation to mark code as generated and thus excluded from code coverage.
 *
 * Note that must contain "Generated" in its name in order for Jacoco to account
 * for it in its coverage computation.  See
 * https://github.com/jacoco/jacoco/issues/976 and
 * https://github.com/jacoco/jacoco/issues/1491.
 *
 * For Kover to account for this class, it is configured in
 * `gradle-settings.gradle.kts`.
 */
annotation class Generated
