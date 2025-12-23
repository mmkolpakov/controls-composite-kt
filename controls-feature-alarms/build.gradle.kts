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
        api(projects.controlsCore)
        api(libs.dataforge.meta)
    }
}