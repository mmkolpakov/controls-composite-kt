import space.kscience.gradle.KScienceNativeTarget
import space.kscience.gradle.Maturity

plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "A universal API for the Magix message bus, defining core contracts for endpoints and messages."

kscience {
    jvm()
    js()
    native {
        setTargets(
            KScienceNativeTarget.linuxX64,
            KScienceNativeTarget.mingwX64,
        )
    }
    wasmJs()
    useCoroutines()
    useSerialization {
        json()
    }

    commonMain {
        api(libs.dataforge.context)
        api(libs.dataforge.meta)
    }
}

readme {
    maturity = Maturity.EXPERIMENTAL
}