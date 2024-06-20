val kotlinVersion = "1.9.23"
plugins {
    val kotlinVersion = "1.9.23"

    application
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version "1.9.22"
    id("com.adarshr.test-logger") version "2.0.0" // coloured test output
}

kotlin {
    jvmToolchain(21)
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

//val exposedVersion: String = "0.50.1" //by project (by project needs the config in the `gradle.properties` file as "exposedVersion=0.50.1")
dependencies {
    implementation(kotlin("reflect"))
    
    // logging
    implementation("ch.qos.logback:logback-classic:1.5.6") // flavour on top of slf4j to make configs for it
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0") // log easily from kotlin
    implementation("org.slf4j:slf4j-api:2.0.13") // basic dependency of kotlin-logging (base logging util)

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.25.10") // For parsing Java files - alternative: TreeSitter
    /* implementation("io.github.bonede:tree-sitter:0.22.6") // TreeSitter - parsing arbitrary languages (but cannot reconstruct source code easily!)
       implementation("io.github.bonede:tree-sitter-java:0.21.0a") // JSON parser for TreeSitter
    */

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3") // For serialization
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")
    testImplementation(kotlin("test")) // For testing

    // Test containers (for running dockerised tests easily)
//    implementation("org.testcontainers:testcontainers-bom:1.19.8") //import bom
//    testImplementation("org.testcontainers:testcontainers:1.19.8")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
}


tasks.test {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        events("started", "skipped", "passed", "failed")
        showStandardStreams = true
    }
    useJUnitPlatform()
}


tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Main-Class" to "org.example.MainKt"))
    }
    from ({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { if (it.isDirectory) it else zipTree(it) }
    }) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

application {
    mainClass = "org.example.MainKt" // needed for `./gradlew run`
}


