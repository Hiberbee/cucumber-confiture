pluginManagement {
  repositories {
    gradlePluginPortal()
  }
}
buildCache {
  local {
    isEnabled = true
    directory = rootProject.projectDir.resolve(".gradle/cache")
  }
}
rootProject.name = "cucumber-confiture"
