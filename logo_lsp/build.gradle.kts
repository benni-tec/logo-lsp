plugins {
    kotlin("jvm")
    application
}

group = "de.benni_tec.logo_lsp"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":logo_antlr"))
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:1.0.0")
}

application {
    mainClass.set("de.benni_tec.logo_lsp.MainKt")
}