plugins {
	kotlin("jvm")
	id("ru.capjack.bintray")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation("ru.capjack.csi:csi-core-server")
	implementation("ru.capjack.tool:tool-utils")
	implementation("ru.capjack.tool:tool-logging")
	api(project(":netty:csi-transport-netty-common"))
}
