import space.kscience.gradle.KScienceNativeTarget
import space.kscience.gradle.Maturity

plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "A ProtocolAdapter implementation for the Modbus protocol using the j2mod library."

kscience {
    jvm()
    useCoroutines()
    useSerialization {
        json()
    }

    jvmMain {
        api(projects.controlsCompositeProtocolApi)
        implementation(libs.j2mod)
    }
}

readme {
    maturity = Maturity.PROTOTYPE
}
