plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
    alias(libs.plugins.kotlinx.atomicfu)
}

description = "Runtime contracts and service interfaces for composite controls"

kscience {
    jvm()
    js()
    native()
    wasmJs()

    useCoroutines()
    useSerialization()

    commonMain {
        api(project(":controls-spec"))
        api(libs.kstatemachine.core)

        api(libs.dataforge.context)
        api(libs.dataforge.data)
        api(libs.dataforge.io)
        implementation(libs.kotlinx.atomicfu)
    }
}

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("ContextParameters")
    }
}
