plugins {
  `java-library`
  idea
  kotlin("jvm") version "1.3.71"
}

group = "com.hiberbee"
version = "1.0.0-SNAPSHOT"

repositories {
  jcenter()
}

tasks.withType<Wrapper>().configureEach {
  gradleVersion = "6.3"
  distributionType = Wrapper.DistributionType.ALL
}

configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_14
  targetCompatibility = JavaVersion.VERSION_14
}

dependencies {
  implementation(kotlin("script-runtime"))
}
