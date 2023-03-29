plugins {
    kotlin("jvm") version "1.8.10"
    application
    id("me.akainth.tape") version "2.1.1"
}

group = "me.akainth"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:multik-core:0.2.1")
    implementation("org.jetbrains.kotlinx:multik-default:0.2.1")

    implementation(files("libs/OpenRocket-22.02.jar"))
}

application {
    mainClass.set("net.sf.openrocket.startup.OpenRocket")
}

tape {
    length.alias("Altitude")
    time
    speed
    acceleration
    mass
    force
    area
    volume
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
sourceSets.getByName("main").java.srcDir(tape.targetDirectory)
