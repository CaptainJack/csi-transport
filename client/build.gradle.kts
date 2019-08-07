plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
	id("ru.capjack.logging")
}

kotlin {
	jvm {
		compilations.all { kotlinOptions.jvmTarget = "1.8" }
	}
	js {
		compilations["main"].kotlinOptions {
			sourceMap = true
			sourceMapEmbedSources = "always"
		}
	}
	
	sourceSets {
		get("commonMain").dependencies {
			implementation(kotlin("stdlib-common"))
			implementation("ru.capjack.tool:tool-logging:0.14.2")
			
			api("ru.capjack.tool:tool-csi-core-client:${project.ext["csiCoreVersion"]}")
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("stdlib-jdk8"))
		}
		
		get("jsMain").dependencies {
			implementation(kotlin("stdlib-js"))
		}
	}
}
