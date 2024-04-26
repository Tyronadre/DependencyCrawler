plugins {
    id("java")
}

group = "de.henrik"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.json:json:20240303")
    implementation("com.google.protobuf:protobuf-java:4.26.1")
    implementation("org.apache.maven:maven-model:4.0.0-alpha-13")
    implementation("org.apache.maven:maven-resolver-provider:4.0.0-alpha-13")
//    implementation("org.apache.maven:maven-resolver-impl:4.0.0-alpha-13")
//    implementation("org.apache.maven:maven-embedder:4.0.0-alpha-13")
    implementation(project(":ProtoParser"))
    implementation("javax.inject:javax.inject:1")
}

tasks.test {
    useJUnitPlatform()
}