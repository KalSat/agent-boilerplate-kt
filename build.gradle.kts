plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    application
}

group = "ai.inspire"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("ai.inspire.MainKt")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("ai.koog:koog-agents:1.0.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    implementation("org.slf4j:slf4j-simple:2.0.17")

    testImplementation(kotlin("test"))
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
