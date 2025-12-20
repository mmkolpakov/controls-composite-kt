plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "Alarms and Events feature"

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