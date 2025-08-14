import space.kscience.gradle.KScienceNativeTarget

plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}

description = "A persistence layer for controls-kt, allowing to save and restore device states."

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
        api(projects.controlsCompositeModel)
        api(libs.dataforge.io)
        implementation(spclibs.kotlinx.coroutines.core)
        implementation("com.squareup.okio:okio:3.15.0")
    }

    commonTest {
        implementation(kotlin("test"))
    }

    wasmJsMain{
        implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3")
        js {
            browser {
                testTask {
                    useKarma {
                        useFirefox()
                        useChrome()
                    }
                }
            }
        }
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.EXPERIMENTAL
}