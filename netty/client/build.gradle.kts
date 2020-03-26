plugins {
	kotlin("jvm")
	id("ru.capjack.bintray")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation("ru.capjack.csi:csi-core-client")
	implementation("ru.capjack.tool:tool-logging")
	api(project(":netty:csi-transport-netty-common"))
}
