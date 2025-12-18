plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "Automation features: Transaction Plans, Sequences, and Scripting support"

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
        api(libs.dataforge.context)
    }
}