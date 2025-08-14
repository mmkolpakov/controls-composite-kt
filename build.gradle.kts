plugins {
    id("space.kscience.gradle.project")
    alias(libs.plugins.ben.manes.versions)
    alias(libs.plugins.kotlinx.atomicfu) apply false
}

allprojects {
    group = "space.kscience"
    version = "1.0.0-alpha-1"
}

readme {
    readmeTemplate = file("docs/templates/README-TEMPLATE.md")
}