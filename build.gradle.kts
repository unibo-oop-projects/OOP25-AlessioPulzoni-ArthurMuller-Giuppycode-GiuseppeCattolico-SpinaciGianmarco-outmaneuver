
plugins {
    // Apply the java plugin to add support for Java
    java

    // Apply the application plugin to add support for building a CLI application
    // You can run your app via task "run": ./gradlew run
    application

    /*
     * Adds tasks to export a runnable jar.
     * In order to create it, launch the "shadowJar" task.
     * The runnable jar will be found in build/libs/projectname-all.jar
     */
    id("com.gradleup.shadow") version "9.4.1"
    id("org.danilopianini.gradle-java-qa") version "1.178.0"

    id("org.danilopianini.unibo-oop-gradle-plugin") version "2.0.22"
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }


repositories { // Where to search for dependencies
    mavenCentral()
}

dependencies {
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.8")
    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.mockito:mockito-core:5.18.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

application {
    mainClass.set("outmaneuver.Main")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform() // Enables the engine of JUnit 5/6
    testLogging { // Additional Options
        // Display all events (test started, succeeded, failed...)
        events(*org.gradle.api.tasks.testing.logging.TestLogEvent.entries.toTypedArray())
        showStandardStreams = true // Show the standard output
    }
}

tasks.withType<Javadoc>().configureEach {
    isFailOnError = false
}
