plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "Integration with DataForge DataTree"

kscience {
    jvm()
    js()
    native()
    wasmJs()

    useCoroutines()

    commonMain {
        api(projects.controlsCore)
        api(libs.dataforge.data)
    }
}