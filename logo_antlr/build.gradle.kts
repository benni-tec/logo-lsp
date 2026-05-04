plugins {
    kotlin("jvm")
    id("com.strumenta.antlr-kotlin")
}

val antlrKotlinVersion: String by project

val generateKotlinGrammarSource = tasks.register<com.strumenta.antlrkotlin.gradle.AntlrKotlinTask>("generateKotlinGrammarSource") {
    source = fileTree(layout.projectDirectory.dir("antlr")) {
        include("**/*.g4")
    }

    val pkgName = "de.benni_tec.logo_antlr"
    packageName = pkgName
    arguments = listOf("-visitor")

    outputDirectory = layout.buildDirectory.dir("generated-sources/antlr").get().asFile
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDir(generateKotlinGrammarSource)
        }
    }
}

dependencies {
    implementation("com.strumenta:antlr-kotlin-runtime:$antlrKotlinVersion")
}