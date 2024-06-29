plugins {
    id("java")
}

group = "de.henrik"
version = "0.9"

repositories {
    mavenCentral()
}

tasks.buildDependents {
    dependsOn("ProtoParser:parseProto")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Implementation-Title" to "Gradle",
            "Implementation-Version" to archiveVersion,
            "Main-Class" to "Main"
        )
    }
    from(sourceSets.main.get().output)
    from(sourceSets.main.get().allSource) {
        into("src")
    }
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
    })
}



dependencies {
    compileOnly("com.google.protobuf:protobuf-java:4.27.0-RC1")
    implementation("com.google.protobuf:protobuf-java-util:4.27.0-RC1")
    implementation("org.apache.maven:maven-model:4.0.0-alpha-13")
    implementation("us.springett:cvss-calculator:1.4.2")
    implementation("org.spdx:java-spdx-library:1.1.11")
    implementation("org.spdx:spdx-jackson-store:1.1.9.1")
    implementation("org.slf4j:slf4j-nop:2.0.0")


    implementation(project(":ProtoParser"))
}
