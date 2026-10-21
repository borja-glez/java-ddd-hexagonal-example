// La aplicación Spring Boot ejecutable (app): cablea todos los bounded contexts.
plugins {
    id("lab.spring-conventions")
    id("org.springframework.boot")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("ddd-hexagonal-lab.jar")
}
