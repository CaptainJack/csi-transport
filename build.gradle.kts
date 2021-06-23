plugins {
	kotlin("multiplatform") version "1.5.10" apply false
	id("ru.capjack.publisher") version "1.0.0"
	id("ru.capjack.depver") version "1.2.0"
}

depver {
	"ru.capjack.tool" {
		"tool-lang"("1.11.1")
		"tool-utils"("1.6.1")
		"tool-io"("1.0.0")
		"tool-logging"("1.5.0")
	}
	"ru.capjack.csi:csi-core-*"("1.0.+")
	"io.netty"("4.1.65.Final")
}

subprojects {
	group = "ru.capjack.csi"
	
	repositories {
		mavenCentral()
		mavenCapjack()
	}
	
	afterEvaluate {
		when {
			plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") -> {
				configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
					targets.forEach {
						if (it is org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget) {
							it.compilations.all { kotlinOptions.jvmTarget = "11" }
						}
					}
				}
			}
			plugins.hasPlugin("org.jetbrains.kotlin.jvm")           -> {
				configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
					target.compilations.all { kotlinOptions.jvmTarget = "11" }
				}
			}
		}
	}
}
