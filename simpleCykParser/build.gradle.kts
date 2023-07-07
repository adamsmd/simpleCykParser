// NOTE: Groups with comment headers are sorted alphabetically by group name (TODO)

description = "Complete But Simple-To-Implement CYK Parsing"

// To see a complete list of tasks, use: ./gradlew tasks
plugins {
  id("gradle-settings")
  id("git-version")
  kotlin("plugin.serialization") version "1.4.20"
  application // Provides "./gradlew installDist" then "./build/install/simpleCykParser/bin/simpleCykParser"
}

// repositories {
//   maven { url = uri("https://dev.bibsonomy.org/maven2/") }
// }

dependencies {
  // Command-line argument parsing
  implementation("com.github.ajalt.clikt:clikt:3.5.1")

  // Logging
  implementation("ch.qos.logback:logback-classic:1.4.5")
  // implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")

  // Test diffs
  implementation("io.github.java-diff-utils:java-diff-utils:4.12")

  // Test-report generation
  implementation("org.junit.platform:junit-platform-reporting:1.9.2")

  // YAML Parsing
  implementation("com.charleskorn.kaml:kaml:0.53.0")
}

// ================================================================== //
// Main
application {
  mainClass.set("org.michaeldadams.simpleCykParser.MainKt")
}
