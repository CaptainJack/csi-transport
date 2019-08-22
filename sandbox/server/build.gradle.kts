plugins {
	kotlin("jvm")
}

version = ""

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation("ch.qos.logback:logback-classic:1.2.3")
	implementation("ru.capjack.tool:tool-logging")
	implementation(project(":tool-csi-transport-server"))
}
