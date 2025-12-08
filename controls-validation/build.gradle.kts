plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "Validation logic for composite device blueprints"

kscience {
    jvm()
    js()
    native()
    wasmJs()

    useCoroutines()
    useSerialization()

    commonMain {
        api(project(":controls-api"))

        implementation(libs.dataforge.context)
        implementation(libs.dataforge.meta)
    }
}
