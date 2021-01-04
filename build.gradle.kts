import me.akainth.tape.GenerateDimensionsTask

plugins {
    kotlin("jvm") version "1.4.21"
    application
    id("me.akainth.tape") version "1.2.1"
}

group = "me.akainth"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(files("libs/OpenRocket.jar"))
}

application {
    mainModule.set("net.sf.openrocket")
    mainClass.set("net.sf.openrocket.startup.SwingStartup")
}

tasks["compileKotlin"].dependsOn(tasks["tape"])
sourceSets["main"].java.srcDir(tasks.getByName("tape", GenerateDimensionsTask::class).targetDirectory)

tasks.getByName("tape", GenerateDimensionsTask::class) {
    length
    time
    speed
    acceleration
    mass
    force
    area
    volume
}
