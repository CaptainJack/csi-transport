plugins {
	kotlin("multiplatform") version "1.3.41" apply false
	id("nebula.release") version "10.1.2"
	id("ru.capjack.bintray") version "0.19.0"
	id("ru.capjack.logging") version "0.14.2" apply false
}

subprojects {
	group = "ru.capjack.tool"
	
	repositories {
		jcenter()
		maven("https://dl.bintray.com/capjack/public")
		mavenLocal()
	}
}
