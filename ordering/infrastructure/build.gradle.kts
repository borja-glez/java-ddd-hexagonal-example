// Anillo exterior: adaptadores (REST, scheduler, JPA, ACL de pagos, eventos) y el cableado de Spring.
// Depende del núcleo; nada del núcleo depende de él.
plugins {
    id("lab.spring-conventions")
}

dependencies {
    implementation(project(":ordering:application"))
    implementation(project(":ordering:contracts"))
    // Contexto upstream. Ordering solo puede usar catalog.api (lo comprueba ArchitectureTest).
    implementation(project(":catalog"))
    // SDK de terceros. Solo la ACL de pagos puede tocarlo (lo comprueba ArchitectureTest).
    implementation(project(":legacy-pay-sdk"))

    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.data.jpa)
}
