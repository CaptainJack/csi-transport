plugins {
	kotlin("js")
	id("ru.capjack.bintray")
}

dependencies {
	implementation(kotlin("stdlib-js"))
	implementation("ru.capjack.tool:tool-lang")
	implementation("ru.capjack.tool:tool-utils")
	implementation("ru.capjack.tool:tool-logging")
	api("ru.capjack.csi:csi-core-client")
}
