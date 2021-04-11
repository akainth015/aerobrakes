import me.akainth.tape.GenerateDimensionsTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
    application
    id("me.akainth.tape") version "1.2.4"
}

group = "me.akainth"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(files("libs/OpenRocket-15.03.jar"))
}

application {
    mainClass.set("net.sf.openrocket.startup.OpenRocket")
}

val tapeTask = tasks.getByName("tape", GenerateDimensionsTask::class) {
    length
    time
    speed
    acceleration
    mass
    force
    area
    volume
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(tapeTask)

    // OpenRocket 15 only works in JRE 1.8
    targetCompatibility = "1.8"
    // https://blog.jetbrains.com/kotlin/2021/02/the-jvm-backend-is-in-beta-let-s-make-it-stable-together/
    kotlinOptions {
        useIR = true
    }
}
sourceSets.getByName("main").java.srcDir(tapeTask.targetDirectory)
