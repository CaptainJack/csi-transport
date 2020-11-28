plugins {
	kotlin("multiplatform") version "1.4.20" apply false
	id("nebula.release") version "15.3.0"
	id("ru.capjack.depver") version "1.2.0"
	id("ru.capjack.bintray") version "1.0.0"
}

depver {
	"ru.capjack.tool" {
		"tool-lang"("1.7.0")
		"tool-utils"("1.1.1")
		"tool-io"("0.8.0")
		"tool-logging"("1.2.2")
	}
	"ru.capjack.csi:csi-core-*"("0.6.0")
	"io.netty"("4.1.54.Final")
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
							it.compilations.all { kotlinOptions.jvmTarget = "1.8" }
						}
					}
				}
			}
			plugins.hasPlugin("org.jetbrains.kotlin.jvm")           -> {
				configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
					target.compilations.all { kotlinOptions.jvmTarget = "1.8" }
				}
			}
		}
	}
}
