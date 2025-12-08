plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "Core primitives and data models for composite controls"

kscience {
    jvm()
    js()
    native()
    wasmJs()

    useCoroutines()
    useSerialization()

    commonMain {
        api(libs.dataforge.meta)
        api(libs.kotlinx.io.core)
        api(libs.kotlinx.datetime)
    }
}
