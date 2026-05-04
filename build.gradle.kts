plugins {
    kotlin("jvm") version "2.3.10" apply false
    id("com.strumenta.antlr-kotlin") version "1.0.10" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}