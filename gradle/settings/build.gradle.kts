// NOTE: Groups with comment headers are sorted alphabetically by group name

group = "org.michaeldadams.simpleCykParser"

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

repositories {
  gradlePluginPortal()
  mavenCentral()
}

// NOTE: Must be kept in sync with gradle-settings.gradle.kts
dependencies {
  // Code Coverage
  // implementation("org.jacoco:jacoco:") // Tasks: jacocoTestReport
  implementation("org.jetbrains.kotlinx.kover:org.jetbrains.kotlinx.kover.gradle.plugin:0.7.2")

  // Dependency Licenses
  implementation("com.github.jk1.dependency-license-report:com.github.jk1.dependency-license-report.gradle.plugin:2.5")

  // Dependency Versions
  implementation("com.github.ben-manes:gradle-versions-plugin:0.47.0")

  // Documentation
  implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.8.20")

  // Kotlin Plugin
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")

  // Linting
  implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.0")
  // implementation("com.ncorti.ktfmt.gradle:0.11.0")
  implementation("org.cqfn.diktat.diktat-gradle-plugin:org.cqfn.diktat.diktat-gradle-plugin.gradle.plugin:1.2.5")
  implementation("org.jlleitschuh.gradle.ktlint:org.jlleitschuh.gradle.ktlint.gradle.plugin:11.5.0")
  implementation("se.solrike.sonarlint:sonarlint-gradle-plugin:1.0.0-beta.15")
}
