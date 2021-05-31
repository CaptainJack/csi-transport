plugins {
	kotlin("jvm")
	id("ru.capjack.publisher")
}

dependencies {
	implementation("ru.capjack.csi:csi-core-common")
	implementation("ru.capjack.tool:tool-logging")
	api("ru.capjack.tool:tool-io")
	api("io.netty:netty-transport")
	api("io.netty:netty-transport-native-epoll")
	api("io.netty:netty-codec-http")
}