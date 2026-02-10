allprojects {
  repositories {
    google()
    mavenCentral()
  }
}

buildscript {
  repositories {
    google()
    mavenCentral()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:7.0.1")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0") // Replace with the appropriate version
    classpath("com.google.gms:google-services:4.3.15")
  }
}

val newBuildDir: Directory = rootProject.layout.buildDirectory.dir("../../build").get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
  val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
  project.layout.buildDirectory.value(newSubprojectBuildDir)
}

subprojects {
  project.evaluationDependsOn(":app")
}

tasks.register<Delete>("clean") {
  delete(rootProject.layout.buildDirectory)
}
