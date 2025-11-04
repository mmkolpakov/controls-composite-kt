import space.kscience.gradle.KScienceNativeTarget
import space.kscience.gradle.Maturity

plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "An implementation of the MagixEndpoint contract using RSocket over WebSockets."

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
        api(libs.ktor.client.websockets)
    }
}

readme {
    maturity = Maturity.PROTOTYPE
}