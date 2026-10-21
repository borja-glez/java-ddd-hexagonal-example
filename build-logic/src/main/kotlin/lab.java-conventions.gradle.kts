// Todo módulo Java: Java 21, UTF-8 y JUnit 5 + AssertJ para los tests.
// NO añade ninguna dependencia de producción: los módulos del núcleo quedan en Java puro.
plugins {
    `java-library`
}

val libs = the<VersionCatalogsExtension>().named("libs")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

base {
    // ":ordering:domain" -> "ordering-domain": nombres de artefacto sin colisiones.
    archivesName.set(project.path.removePrefix(":").replace(':', '-'))
}

dependencies {
    testImplementation(platform(libs.findLibrary("spring-boot-dependencies").get()))
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testImplementation(libs.findLibrary("assertj-core").get())
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:unchecked,deprecation,rawtypes"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
