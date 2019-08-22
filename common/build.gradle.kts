plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
}


kotlin {
	jvm()
	
	sourceSets {
		get("commonMain").dependencies {
			implementation(kotlin("stdlib-common"))
			implementation("ru.capjack.csi:csi-core-common")
			implementation("ru.capjack.tool:tool-logging")
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("stdlib-jdk8"))
			implementation("io.netty:netty-transport")
			implementation("io.netty:netty-codec-http")
		}
	}
}
