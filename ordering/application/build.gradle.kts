// Núcleo: casos de uso y puertos. Java puro que solo ve el dominio.
plugins {
    id("lab.java-conventions")
}

dependencies {
    api(project(":ordering:domain"))
}
