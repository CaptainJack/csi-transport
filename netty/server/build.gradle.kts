plugins {
	kotlin("jvm")
	id("ru.capjack.publisher")
}

dependencies {
	implementation("ru.capjack.csi:csi-core-server")
	implementation("ru.capjack.tool:tool-utils")
	implementation("ru.capjack.tool:tool-logging")
	api(project(":netty:csi-transport-netty-common"))
}
