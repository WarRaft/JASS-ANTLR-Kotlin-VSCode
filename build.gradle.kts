plugins {
    java

    // https://kotlinlang.org/docs/gradle-configure-project.html
    kotlin("jvm") version "2.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    //implementation("io.github.warraft:jass-antlr:0.0.20")
    implementation(files("/Users/nazarpunk/IdeaProjects/JASS-ANTLR-Kotlin/build/libs/jass-antlr.jar"))
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.23.1")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.23.1")
    implementation(kotlin("test"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")

    archiveBaseName.set("jass")
    archiveVersion.set("antlr-lsp")
}

tasks.test {
    useJUnitPlatform()
}
