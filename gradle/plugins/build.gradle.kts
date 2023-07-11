plugins {
  id("gradle-settings")
  `java-gradle-plugin`
}

dependencies {
  // Must match `ktlint { version.set }` in gradle-settings.gradle.kts
  implementation("com.pinterest.ktlint:ktlint-ruleset-standard:0.49.1")
  implementation("com.pinterest.ktlint:ktlint-core:0.49.1")
  implementation("org.eclipse.jgit:org.eclipse.jgit:6.6.0.202305301015-r")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
}

gradlePlugin {
  plugins.register("git-version") {
    id = "git-version"
    implementationClass = "org.michaeldadams.simpleCykParser.GitVersionPlugin"
  }
}
