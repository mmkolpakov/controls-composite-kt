plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
    alias(libs.plugins.kotlinx.atomicfu)
}

kscience {
    jvm(); js(); native(); wasmJs()
    useCoroutines()

    commonMain {
        api(projects.controlsModel)
        implementation(libs.kotlinx.atomicfu)
    }
}