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
			implementation("io.netty:netty-handler:${project.ext["nettyVersion"]}")
			implementation("io.netty:netty-codec-http:${project.ext["nettyVersion"]}")
			implementation("io.netty:netty-transport:${project.ext["nettyVersion"]}")
			implementation("io.netty:netty-transport-native-epoll:${project.ext["nettyVersion"]}")
			implementation("ru.capjack.tool:tool-logging:0.14.2")
			
			api("ru.capjack.tool:tool-csi-core-server:${project.ext["csiCoreVersion"]}")
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("stdlib-jdk8"))
		}
	}
}
