plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "Declarative specifications and data models for composite controls"

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
