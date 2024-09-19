plugins {
    antlr
    java
    `java-library`
    kotlin("jvm")
    `maven-publish`
    signing
}

group = "com.morizero"
version = "1.0.2"

dependencies {
    antlr("org.antlr:antlr4:4.13.1")
}

kotlin {
    jvmToolchain(11)
}

tasks {
    generateGrammarSource {
        dependsOn("sourcesJar")

        maxHeapSize = "64m"
        arguments = arguments + listOf("-visitor", "-long-messages")
        outputDirectory = outputDirectory.resolve("com/morizero/h2cs/generated/parser")
    }

    compileKotlin {
        dependsOn(generateGrammarSource)
    }

    compileJava {
        dependsOn(generateGrammarSource)
    }

    test {
        useJUnitPlatform()
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

project.ext.set("POM_NAME", "ParaParty H2CS")
project.ext.set("POM_DESCRIPTION", "ParaParty H2CS")
project.ext.set("ARTIFACT_ID", "h2cs")

publishing {
    publications {
        create<MavenPublication>("maven") {

            groupId = project.group.toString()
            version = project.version.toString()
            artifactId = ext.properties["ARTIFACT_ID"]?.toString() ?: project.name

            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(ext.properties["POM_NAME"]?.toString() ?: project.name)
                description.set(ext.properties["POM_DESCRIPTION"]?.toString() ?: project.name)
                url.set("https://pkg.para.party/H2CS")
                developers {
                    developer {
                        id.set("ericlian")
                        name.set("Eric_Lian")
                        email.set("public@superexercisebook.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/ParaParty/H2CS.git")
                    developerConnection.set("scm:git:https://github.com/ParaParty/H2CS.git")
                    url.set("https://github.com/ParaParty/H2CS")
                }
            }
        }
    }
    repositories {
        maven {
            // change URLs to point to your repos, e.g. http://my.org/repo
            val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
            val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
//            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
//            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
//            credentials {
//                username = (findProperty("ossrhUsername") ?: System.getenv("OSSRH_USERNAME")).toString()
//                password = (findProperty("ossrhPassword") ?: System.getenv("OSSRH_PASSWORD")).toString()
//            }

            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

signing {
    sign(publishing.publications)
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
