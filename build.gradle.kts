plugins {
    id("java")
}

group = "de.henrik"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:4.27.0-RC1")
    implementation("com.google.protobuf:protobuf-java-util:4.27.0-RC1")
    implementation("org.apache.maven:maven-model:4.0.0-alpha-13")
    implementation("org.apache.maven:maven-resolver-provider:4.0.0-alpha-13")
    implementation("us.springett:cvss-calculator:1.4.2")
    implementation("org.spdx:java-spdx-library:1.1.11")
    implementation("org.spdx:spdx-jackson-store:1.1.9.1")

    implementation("com.google.firebase:firebase-admin:8.1.0")


    implementation(project(":ProtoParser"))
}

tasks.test {
    useJUnitPlatform()
}