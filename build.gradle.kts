plugins {
	kotlin("multiplatform") version "1.4.31" apply false
	id("nebula.release") version "15.3.1"
	id("ru.capjack.depver") version "1.2.0"
	id("ru.capjack.bintray") version "1.0.0"
}

depver {
	"ru.capjack.tool" {
		"tool-lang"("1.8.0")
		"tool-utils"("1.4.1")
		"tool-io"("0.10.0")
		"tool-logging"("1.3.0")
	}
	"ru.capjack.csi:csi-core-*"("0.7.0-SNAPSHOT")
	"io.netty"("4.1.59.Final")
}

subprojects {
	group = "ru.capjack.csi"
	
	repositories {
		jcenter()
		maven("https://dl.bintray.com/capjack/public")
		mavenLocal()
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
