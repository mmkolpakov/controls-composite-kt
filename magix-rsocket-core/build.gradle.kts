import space.kscience.gradle.KScienceNativeTarget

plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "Core logic for Magix RSocket endpoints."

kscience {
    jvm()
    js()
    native {
        setTargets(
            KScienceNativeTarget.linuxX64,
            KScienceNativeTarget.mingwX64
        )
    }
    wasmJs()
    useCoroutines()
    useSerialization()

    commonMain {
        api(projects.magixApi)
        api(libs.rsocket.ktor.client)
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.PROTOTYPE
}