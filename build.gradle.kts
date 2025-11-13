plugins {
	java
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "9.22.3"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Final Project Dashboard Mini Internet Banking ODP BNI 343"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.auth0:java-jwt:4.4.0")
	implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.soabase.record-builder:record-builder-core:47")
	// --- Metrics for Prometheus ---
	implementation("io.micrometer:micrometer-registry-prometheus")
	// --- Distributed tracing (adds traceId/spanId automatically) ---
	implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")

	// --- JSON logging (for LTM / ELK / Loki)
	implementation("net.logstash.logback:logstash-logback-encoder:7.4")


    implementation("com.oracle.database.jdbc:ojdbc11:23.4.0.24.05")

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-oracle:11.7.2")

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    compileOnly("io.soabase.record-builder:record-builder-core:47")
    annotationProcessor("io.soabase.record-builder:record-builder-processor:47")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.assertj:assertj-core:3.26.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
