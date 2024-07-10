plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    application
}

dependencies {
    api(project(":h2cs"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass = "com.morizero.h2cs.Main"
}

tasks.shadowJar {

}
