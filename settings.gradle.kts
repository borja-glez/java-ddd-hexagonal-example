pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral()
    }
}

rootProject.name = "ddd-hexagonal-lab"

include(
    // Shared Kernel: el trozo de modelo que comparten Catalog y Ordering (Money).
    ":shared-kernel",
    // Bounded context Catalog (subdominio supporting): capas clásicas, a propósito.
    ":catalog",
    // Bounded context Ordering (subdominio core): DDD táctico + hexagonal, un módulo por anillo.
    ":ordering:domain",
    ":ordering:application",
    ":ordering:contracts",
    ":ordering:infrastructure",
    // Bounded context Notifications (subdominio generic): conformista con los eventos de Ordering.
    ":notifications",
    // Simula una librería de terceros (pasarela de pagos legada) que Ordering aísla con una ACL.
    ":legacy-pay-sdk",
    // Arranque: Spring Boot, configuración y tests de arquitectura y end-to-end.
    ":app",
)
