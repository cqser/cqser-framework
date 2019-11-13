import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    kotlin("jvm") version "1.3.11"
    maven
    `maven-publish`
    jacoco
    id("com.jfrog.bintray") version "1.8.1"
    id("org.jetbrains.dokka") version "0.9.17"
}

group = "io.cqser"
version = "0.9.1-RELEASE"

repositories {
    jcenter()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:2.1.9.RELEASE"))

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.springframework:spring-context")
    implementation("org.springframework.data:spring-data-jpa")
    implementation("org.hibernate:hibernate-core")
    implementation("javax.validation:validation-api")
    implementation("javax.transaction:javax.transaction-api")
    implementation("javax.transaction:javax.transaction-api")
    implementation("javax.persistence:javax.persistence-api")
    implementation("org.slf4j:slf4j-api")

    testImplementation(kotlin("test-junit"))
    testImplementation(group = "org.springframework", name = "spring-test", version = "5.1.4.RELEASE")
    testImplementation(group = "org.mockito", name = "mockito-core", version = "2.23.4")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val sourceJar = task("sourceJar", Jar::class) {
    dependsOn(tasks["classes"])
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

val javadocJar = task("javadocJar", Jar::class) {
    val javadoc = tasks["dokka"] as DokkaTask
    javadoc.outputFormat = "javadoc"
    javadoc.outputDirectory = "$buildDir/javadoc"
    dependsOn(javadoc)
    classifier = "javadoc"
    from(javadoc.outputDirectory)
}

publishing {
    publications {
        create("mavenJava", MavenPublication::class.java).apply {
            groupId = project.group.toString()
            this.artifactId = artifactId
            version = project.version.toString()
            pom {
                description.set("Implementation of CQS pattern using Mediatr for JVM and Spring Framework")
                name.set("cqs-starter-spring-data-jpa")
                url.set("https://github.com/cqser/cqser-framework")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("rdehuyss")
                        name.set("Ronald Dehuysser")
                        email.set("ronald@rosoco.be")
                    }
                }
                scm {
                    url.set("https://github.com/cqser/cqser-framework")
                }
            }

            from(components["java"])
            artifact(sourceJar)

        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_API_KEY")
    pkg(closureOf<BintrayExtension.PackageConfig> {
        userOrg = "cqser"
        repo = "CQSER-maven"
        name = "cqser-spring-data-jpa"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/cqser/cqser-framework"
        publish = true
        setPublications("mavenJava")
        version(closureOf<BintrayExtension.VersionConfig> {
            this.name = project.version.toString()
            released = Date().toString()
        })
    })
}