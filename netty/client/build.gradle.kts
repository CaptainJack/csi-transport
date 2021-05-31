plugins {
	kotlin("jvm")
	id("ru.capjack.publisher")
}

dependencies {
	implementation("ru.capjack.csi:csi-core-client")
	implementation("ru.capjack.tool:tool-logging")
	api(project(":netty:csi-transport-netty-common"))
}
