plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
}


kotlin {
	jvm {
		compilations.all { kotlinOptions.jvmTarget = "1.8" }
	}
	
	sourceSets {
		get("commonMain").dependencies {
			implementation(kotlin("stdlib-common"))
			implementation("io.netty:netty-handler")
			implementation("io.netty:netty-codec-http")
			implementation("io.netty:netty-transport")
			implementation("io.netty:netty-transport-native-epoll")
			implementation("ru.capjack.tool:tool-logging")
			
			api("ru.capjack.tool:tool-csi-core-server")
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("stdlib-jdk8"))
		}
	}
}
