plugins {
	kotlin("multiplatform")
}

kotlin {
	jvm()
	js(IR) {
		browser()
	}
	
	sourceSets {
		get("commonMain").dependencies {
			api("ru.capjack.csi:csi-core-client")
			api("ru.capjack.tool:tool-logging")
		}
	}
}
