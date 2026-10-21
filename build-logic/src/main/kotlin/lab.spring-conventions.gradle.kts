// Módulos del anillo exterior (adaptadores, contextos sencillos): aquí Spring sí está permitido.
plugins {
    id("lab.java-conventions")
}

val libs = the<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(platform(libs.findLibrary("spring-boot-dependencies").get()))
}
