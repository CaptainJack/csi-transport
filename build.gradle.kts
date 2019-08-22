import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository

plugins {
	kotlin("multiplatform") version "1.3.41" apply false
	id("ru.capjack.depver") version "0.2.2"
	id("ru.capjack.logging") version "0.14.7"
	id("ru.capjack.bintray") version "0.20.1"
	id("nebula.release") version "11.1.0"
}

depver {
	"ru.capjack.tool:tool-csi-core-*"("0.2.0-dev.2.uncommitted+000ca18")
	"io.netty"("4.1.39.Final")
}

subprojects {
	group = "ru.capjack.tool"
	
	repositories {
		jcenter()
		maven("https://dl.bintray.com/capjack/public")
		mavenLocal()
	}
}
