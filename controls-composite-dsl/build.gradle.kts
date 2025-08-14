import space.kscience.gradle.KScienceNativeTarget

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
    alias(libs.plugins.kotlinx.atomicfu)
}

description = "A type-safe Kotlin DSL for building composite device specifications."

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
        api(libs.dataforge.meta)
        api(libs.kstatemachine.core)
        api(projects.controlsCompositeModel)
    }

    commonTest {
        implementation(kotlin("test"))
        implementation(spclibs.logback.classic)
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.PROTOTYPE
}