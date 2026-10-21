// Composition root: un único desplegable que contiene todos los bounded contexts (monolito modular).
plugins {
    id("lab.boot-application")
}

dependencies {
    implementation(project(":catalog"))
    implementation(project(":ordering:infrastructure"))
    implementation(project(":notifications"))

    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.h2)

    // Los tests end-to-end leen el registro del sandbox de pagos y los emails enviados.
    testImplementation(project(":legacy-pay-sdk"))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.archunit.junit5)
}
