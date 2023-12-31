// NOTE: Groups with comment headers are sorted alphabetically by group name (TODO)

description = "Complete But Simple-To-Implement CYK Parsing"

// To see a complete list of tasks, use: ./gradlew tasks
plugins {
  id("gradle-settings")
  id("git-version")
  // Provides "./gradlew installDist" then "./build/install/simpleCykParser/bin/simpleCykParser"
  application
  id("com.dorongold.task-tree") version "2.1.1"
}

// repositories {
//   maven { url = uri("https://dev.bibsonomy.org/maven2/") }
// }

dependencies {
  // Command-line argument parsing
  implementation("com.github.ajalt.clikt:clikt:4.0.0")

  // Logging
  implementation("ch.qos.logback:logback-classic:1.4.8")
  // implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")

  // Test-report generation
  implementation("org.junit.platform:junit-platform-reporting:1.9.3")

  // YAML Parsing
  implementation("com.charleskorn.kaml:kaml:0.54.0")
  implementation("org.snakeyaml:snakeyaml-engine:2.6")
}

// ================================================================== //
// Main
application {
  mainClass.set("org.michaeldadams.simpleCykParser.main.MainKt")
}
