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
			implementation("ru.capjack.tool:tool-logging")
			
			api("ru.capjack.tool:tool-csi-core-client")
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("stdlib-jdk8"))
		}
		
		get("jsMain").dependencies {
			implementation(kotlin("stdlib-js"))
		}
	}
}
