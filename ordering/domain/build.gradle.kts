// Núcleo: el modelo de dominio de Ordering. Java puro cuya ÚNICA dependencia es el Shared Kernel.
// Spring, JPA y Jackson no están en el classpath de este módulo: importarlos no compila.
plugins {
    id("lab.java-conventions")
}

dependencies {
    api(project(":shared-kernel"))
}
