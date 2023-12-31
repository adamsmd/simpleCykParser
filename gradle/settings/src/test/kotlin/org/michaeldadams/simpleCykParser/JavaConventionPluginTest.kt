// package com.example

// import org.gradle.testkit.runner.TaskOutcome
// import org.junit.Assert.assertEquals
// import org.junit.Assert.assertTrue
// import org.junit.Before
// import org.junit.Test

// class JavaConventionPluginTest : PluginTest() {

//     @Before
//     fun init() {
//         buildFile.appendText("""
//             plugins {
//                 id("myproject.java-conventions")
//             }
//         """)
//     }

//     @Test
//     fun `fails on checkstyle error`() {
//         testProjectDir.newFolder("src", "main", "java", "com", "example")
//         testProjectDir.newFile("src/main/java/com/example/Foo.java").appendText("""
//             package com.example;

//             import java.util.*;

//             class Foo {
//                 void bar() {
//                 }
//             }
//         """)

//         val result = runTaskWithFailure("build")

//         assertEquals(TaskOutcome.FAILED, result.task(":checkstyleMain")?.outcome)
//         assertTrue(result.output.contains("Checkstyle rule violations were found."))
//         assertTrue(result.output.contains("Checkstyle violations by severity: [error:1]"))
//     }

//     @Test
//     fun `fails on checkstyle warning`() {
//         testProjectDir.newFolder("src", "main", "java", "com", "example")
//         testProjectDir.newFile("src/main/java/com/example/Foo.java").writeText("""
//             package com.example;

//             class Foo {
//                 final static public String FOO = "BAR";

//                 void bar() {
//                 }
//             }
//         """)

//         val result = runTaskWithFailure("build")

//         assertEquals(TaskOutcome.FAILED, result.task(":checkstyleMain")?.outcome)
//         assertTrue(result.output.contains("Checkstyle rule violations were found."))
//         assertTrue(result.output.contains("Checkstyle violations by severity: [warning:1]"))
//     }

//     @Test
//     fun `fails on spotbugs error`() {
//         testProjectDir.newFolder("src", "main", "java", "com", "example")
//         testProjectDir.newFile("src/main/java/com/example/Foo.java").writeText("""
//             package com.example;

//             class Foo {
//                 void bar() {
//                     String s = null;
//                     s.hashCode();
//                 }
//             }
//         """)

//         val result = runTaskWithFailure("build")

//         assertEquals(TaskOutcome.FAILED, result.task(":spotbugsMain")?.outcome)
//     }

//     @Test
//     fun `warns on deprecated API usage`() {
//         testProjectDir.newFolder("src", "main", "java", "com", "example")
//         testProjectDir.newFile("src/main/java/com/example/Foo.java").writeText("""
//             package com.example;

//             public class Foo {
//                 @Deprecated
//                 public void deprecatedMethod() {}
//             }
//         """)

//         testProjectDir.newFile("src/main/java/com/example/Bar.java").writeText("""
//             package com.example;

//             public class Bar {
//                 public void bar() {
//                     new Foo().deprecatedMethod();
//                 }
//             }
//         """)

//         val result = runTask("build")

//         assertEquals(TaskOutcome.SUCCESS, result.task(":build")?.outcome)
//         assertTrue(result.output.contains("warning: [deprecation] deprecatedMethod()"))
//     }
// }
