plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "Connectivity features: Child composition, Peer connections, and Property bindings"

kscience {
    jvm()
    js()
    native()
    wasmJs()

    useCoroutines()
    useSerialization()

    commonMain {
        api(project(":controls-core"))
        api(project(":controls-feature-fsm"))
        api(libs.dataforge.meta)
        api(libs.dataforge.context)
        api(libs.dataforge.io)
    }
}