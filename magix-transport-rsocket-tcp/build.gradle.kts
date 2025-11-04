import space.kscience.gradle.KScienceNativeTarget
import space.kscience.gradle.Maturity

plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "An implementation of the MagixEndpoint contract using RSocket over TCP."

kscience {
    jvm()
    native {
        setTargets(
            KScienceNativeTarget.linuxX64,
            KScienceNativeTarget.mingwX64
        )
    }
    js { browser(); nodejs() }
    wasmJs { browser() }

    useCoroutines()
    useSerialization {
        json()
    }

    commonMain {
        api(projects.magixApi)
        api(projects.magixRsocketCore)
        api(libs.dataforge.context)
        api(libs.dataforge.meta)
        api(libs.rsocket.ktor.client)
    }

    jvmMain {
        api(libs.rsocket.transport.ktor.tcp)
    }

    nativeMain {
        api(libs.rsocket.transport.ktor.tcp)
    }
}

readme {
    maturity = Maturity.PROTOTYPE
}