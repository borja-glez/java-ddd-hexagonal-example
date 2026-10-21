// Catalog: subdominio supporting sin reglas que proteger, así que capas clásicas a propósito.
plugins {
    id("lab.spring-conventions")
}

dependencies {
    // Money viene del Shared Kernel y forma parte del ProductView publicado.
    api(project(":shared-kernel"))

    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.data.jpa)
}
