plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.lemon"
version = "0.0.1-SNAPSHOT"
description = "lemonade"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation("com.google.api-client:google-api-client:2.2.0")
    implementation("com.google.http-client:google-http-client-jackson2:1.43.3")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
}

dependencies {
    testImplementation("org.springframework.security:spring-security-test")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
}
