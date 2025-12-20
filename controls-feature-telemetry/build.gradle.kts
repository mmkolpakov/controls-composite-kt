plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "High-performance telemetry (data plane) features"

kscience {
    jvm()
    js()
    native()
    wasmJs()

    useCoroutines()
    useSerialization()

    commonMain {
        api(project(":controls-core"))
        api(libs.dataforge.meta)
    }
}