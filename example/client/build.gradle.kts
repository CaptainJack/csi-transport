plugins {
	kotlin("js")
}

apply {
	plugin("kotlin-dce-js")
}

dependencies {
	implementation(kotlin("stdlib-js"))
	implementation(project(":tool-csi-transport-client"))
	implementation("ru.capjack.tool:tool-logging:0.14.2")
}


val webOutputDir = "build/web"

task<Copy>("buildWeb-js") {
	dependsOn("runDceKotlin")
	from("build/kotlin-js-min/main")
	into("$webOutputDir/js")
}

task<Copy>("buildWeb-resources") {
	from("src/main/resources")
	into(webOutputDir)
}

task("buildWeb") {
	group = "build"
	dependsOn(
		"buildWeb-js",
		"buildWeb-resources"
	)
}