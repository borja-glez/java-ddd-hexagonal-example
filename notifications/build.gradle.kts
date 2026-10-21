// Notifications: subdominio generic. Se conforma con el lenguaje publicado de Ordering y no sabe
// nada más de Ordering.
plugins {
    id("lab.spring-conventions")
}

dependencies {
    implementation(project(":ordering:contracts"))
    implementation(libs.spring.boot.starter)
}
