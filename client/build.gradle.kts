plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
	id("ru.capjack.logging")
}

kotlin {
	jvm()
	js()
	
	sourceSets {
		get("commonMain").dependencies {
			implementation(kotlin("stdlib-common"))
			implementation("ru.capjack.csi:csi-core-client")
			implementation("ru.capjack.tool:tool-logging")
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("stdlib-jdk8"))
			implementation(project(":csi-transport-common"))
			implementation("io.netty:netty-transport")
			implementation("io.netty:netty-transport-native-epoll")
			implementation("io.netty:netty-codec-http")
		}
		
		get("jsMain").dependencies {
			implementation(kotlin("stdlib-js"))
			implementation("ru.capjack.tool:tool-lang")
			implementation("ru.capjack.tool:tool-utils")
		}
	}
}
