plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
}

kotlin {
	js(IR) {
		browser()
	}
	
	sourceSets {
		get("commonMain").dependencies {
			implementation("ru.capjack.tool:tool-lang")
			implementation("ru.capjack.tool:tool-utils")
			implementation("ru.capjack.tool:tool-logging")
			api("ru.capjack.csi:csi-core-client")
		}
	}
}
