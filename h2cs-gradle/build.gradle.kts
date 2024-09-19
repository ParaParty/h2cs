plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `java-library`
    `maven-publish`
    signing
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

gradlePlugin {
    plugins {
        create("h2cs") {
            id = "party.para.h2cs"
            implementationClass = "party.para.H2CSPlugin"
        }
    }
}

project.ext.set("POM_NAME", "ParaParty H2CS Gradle")
project.ext.set("POM_DESCRIPTION", "ParaParty H2CS Gradle")
project.ext.set("ARTIFACT_ID", "h2cs-gradle")

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

afterEvaluate {
    tasks {
        val publishMavenPublicationToMavenRepository =
            getByName<DefaultTask>("publishMavenPublicationToMavenRepository")
        val publishMavenPublicationToMavenLocal =
            getByName<DefaultTask>("publishMavenPublicationToMavenLocal")

        getByName<DefaultTask>("signPluginMavenPublication") {
            dependsOn(publishMavenPublicationToMavenRepository)
            dependsOn(publishMavenPublicationToMavenLocal)
        }
    }
}
