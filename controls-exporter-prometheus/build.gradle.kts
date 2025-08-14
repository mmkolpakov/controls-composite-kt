import space.kscience.gradle.KScienceNativeTarget

plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "A KMP-native Prometheus exporter for controls-kt metrics."

kscience {
    jvm()
    js { browser(); nodejs() }
    native {
        setTargets(
            KScienceNativeTarget.linuxX64,
            KScienceNativeTarget.mingwX64
        )
    }
    wasmJs { browser() }

    commonMain {
        api(projects.controlsCompositeMetrics)
        implementation(libs.kotlinx.atomicfu)
    }

    jvmMain {
        implementation(libs.ktor.server.netty)
    }
}