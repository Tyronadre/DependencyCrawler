group = "de.henrik"
version = "1.0"

plugins {
    java
}

repositories {
    mavenCentral()
}

sourceSets {
    main {
        java.srcDir("src/out")
    }
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:4.27.0-RC1")
}

tasks.register<Exec>("parseProto") {
    workingDir = file("src")
    if (!file("src/protoc/bin/protoc.exe").exists())
        throw GradleException("protoc not found. Please download the protoc parser first from https://github.com/protocolbuffers/protobuf/releases/tag/v27.0-rc1 and save it to ProtoParser::src/protoc/bin/protoc.exe")

    try {
        commandLine("bash", "-c", "./run.sh")
    } catch (e: Exception) {
        throw GradleException("Error while executing protoc parser: ${e.message}")
    }
}