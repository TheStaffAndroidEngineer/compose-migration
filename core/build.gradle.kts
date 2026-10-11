plugins { alias(libs.plugins.kotlinJvm) }
kotlin {
    jvmToolchain(27)
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_26) }
}
dependencies {
    testImplementation(libs.junitJupiter)
    testRuntimeOnly(libs.junitPlatformLauncher)
}
tasks.test { useJUnitPlatform() }

tasks.register<JavaExec>("stateTrace") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("staff.android.saveditems.StateTraceKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_26
    targetCompatibility = JavaVersion.VERSION_26
}
tasks.withType<JavaCompile>().configureEach { options.release.set(26) }
