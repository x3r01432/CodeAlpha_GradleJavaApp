plugins {
    application
    jacoco
    alias(libs.plugins.shadow)
}

group = "com.codealpha"
version = "1.0.0"

repositories { mavenCentral() }

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

dependencies {
    implementation(libs.gson)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}

application { mainClass = "com.codealpha.todo.App" }

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco { toolVersion = "0.8.12" }

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports { xml.required = true; html.required = true }
}

tasks.jacocoTestCoverageVerification {
    violationRules { rule { limit { minimum = "0.70".toBigDecimal() } } }
}
tasks.check { dependsOn(tasks.jacocoTestCoverageVerification) }

tasks.shadowJar {
    archiveBaseName = "todo-api"
    archiveClassifier = "all"
    archiveVersion = project.version.toString()
}
