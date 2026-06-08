plugins {
    kotlin("jvm") version "2.3.21"
}

group = "ai.inspire"
version = "1.0-SNAPSHOT"

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
