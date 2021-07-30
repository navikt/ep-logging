import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.21"
    `java-library`
    id("net.researchgate.release") version "2.8.1"
    `maven-publish`
    id("com.adarshr.test-logger") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.spring") version "1.5.21"
    id("com.github.ben-manes.versions") version "0.28.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.13"
    id("org.owasp.dependencycheck") version "5.3.2.1"
}

group = "no.nav.eessi.pensjon"

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val oidcTokenSupportVersion = "1.1.6"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("no.nav.security:token-validation-spring:${oidcTokenSupportVersion}")
    implementation("org.springframework.boot:spring-boot-starter-web:2.5.3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.+")
    testImplementation("org.mockito:mockito-junit-jupiter:3.2.4")
    testImplementation("org.springframework:spring-test:5.+")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}

// https://github.com/researchgate/gradle-release
release {
    newVersionCommitMessage = "[Release Plugin] - next version commit: "
    tagTemplate = "release-\${version}"
}

// https://help.github.com/en/actions/language-and-framework-guides/publishing-java-packages-with-gradle#publishing-packages-to-github-packages
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/navikt/${rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

/* https://github.com/ben-manes/gradle-versions-plugin */
tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        listOf("alpha", "beta", "rc", "cr", "m", "preview", "pr2")
                .any { qualifier -> """(?i).*[.-]${qualifier}[.\d-]*""".toRegex().matches(candidate.version) }
    }
    revision = "release"
}
