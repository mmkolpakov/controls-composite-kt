plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "Type-safe DSL for building composite device blueprints and specifications"

kscience {
    jvm()
    js()
    native()
    wasmJs()

    useCoroutines()
    useSerialization()

    commonMain {
        api(project(":controls-core"))
        api(project(":controls-spec"))
        api(project(":controls-api"))
        api(project(":controls-validation"))

        api(libs.dataforge.meta)
        api(libs.kstatemachine.core)
    }

    commonTest {
        implementation(kotlin("test"))
        implementation(spclibs.logback.classic)
    }
}
