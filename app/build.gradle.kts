import com.google.devtools.ksp.gradle.KspTaskJvm
import com.mikepenz.aboutlibraries.plugin.DuplicateMode.MERGE
import com.mikepenz.aboutlibraries.plugin.DuplicateRule.GROUP
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

val isRelease: Boolean
    get() = gradle.startParameter.taskNames.any { it.contains("Release") }

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.mikepenz.aboutlibraries.plugin")
    id("org.mozilla.rust-android-gradle.rust-android")
}

android {
    compileSdk = 33
    buildToolsVersion = "33.0.2"
    ndkVersion = "25.2.9519653"

    splits {
        abi {
            isEnable = true
            reset()
            if (isRelease) {
                include("arm64-v8a", "x86_64", "armeabi-v7a", "x86")
                isUniversalApk = true
            } else {
                include("arm64-v8a")
            }
        }
    }

    val signConfig = signingConfigs.create("release") {
        storeFile = File(projectDir.path + "/keystore/androidkey.jks")
        storePassword = "000000"
        keyAlias = "key0"
        keyPassword = "000000"
        enableV3Signing = true
        enableV4Signing = true
    }

    val commitSha by lazy {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine = "git rev-parse --short=7 HEAD".split(' ')
            standardOutput = stdout
        }
        stdout.toString().trim()
    }

    val buildTime by lazy {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(ZoneOffset.UTC)
        formatter.format(Instant.now())
    }

    defaultConfig {
        applicationId = "moe.tarsin.ehviewer"
        minSdk = 28
        targetSdk = 33
        versionCode = 180042
        versionName = "1.8.9.0-SNAPSHOT"
        resourceConfigurations.addAll(
            listOf(
                "zh",
                "zh-rCN",
                "zh-rHK",
                "zh-rTW",
                "es",
                "ja",
                "ko",
                "fr",
                "de",
                "th",
                "tr",
                "nb-rNO",
            ),
        )
        buildConfigField("String", "COMMIT_SHA", "\"$commitSha\"")
        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")
    }

    externalNativeBuild {
        cmake {
            path = File("src/main/cpp/CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        languageVersion = "2.0"
        freeCompilerArgs = listOf(
            // https://kotlinlang.org/docs/compiler-reference.html#progressive
            "-progressive",
            "-Xjvm-default=all",
            "-Xlambdas=indy",

            "-opt-in=coil.annotation.ExperimentalCoilApi",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=androidx.paging.ExperimentalPagingApi",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=splitties.experimental.ExperimentalSplittiesApi",
            "-opt-in=splitties.preferences.DataStorePreferencesPreview",
        )
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
        disable.add("MissingTranslation")
    }

    packaging {
        resources {
            excludes += "/META-INF/**"
            excludes += "/kotlin/**"
            excludes += "**.txt"
            excludes += "**.bin"
        }
    }

    dependenciesInfo.includeInApk = false

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
            signingConfig = signConfig
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7-dev-k1.9.0-Beta-bb7dc8b44eb"
    }

    namespace = "com.hippo.ehviewer"
}

dependencies {
    // https://developer.android.com/jetpack/androidx/releases/activity
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")
    implementation("androidx.browser:browser:1.5.0")
    implementation("androidx.collection:collection-ktx:1.3.0-alpha04")

    // https://developer.android.com/jetpack/androidx/releases/compose-material3
    // api(platform("androidx.compose:compose-bom:2023.05.00"))
    api(platform("dev.chrisbanes.compose:compose-bom:2023.04.00-SNAPSHOT"))
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")

    implementation("androidx.core:core-ktx:1.11.0-beta02")

    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0-alpha10")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    // https://developer.android.com/jetpack/androidx/releases/lifecycle
    implementation("androidx.lifecycle:lifecycle-process:2.6.1")

    // https://developer.android.com/jetpack/androidx/releases/navigation
    val nav_version = "2.7.0-beta01"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // https://developer.android.com/jetpack/androidx/releases/paging
    implementation("androidx.paging:paging-compose:3.2.0-beta01")
    implementation("androidx.recyclerview:recyclerview:1.3.0")

    // https://developer.android.com/jetpack/androidx/releases/room
    val room_version = "2.6.0-alpha01"
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-paging:$room_version")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("com.drakeet.drawer:drawer:1.0.3")
    implementation("com.github.chrisbanes:PhotoView:2.3.0") // Dead Dependency
    implementation("com.github.tachiyomiorg:DirectionalViewPager:1.0.0") // Dead Dependency
    implementation("com.github.nanihadesuka:LazyColumnScrollbar:1.6.3")
    // https://github.com/google/accompanist/releases
    val accompanist_version = "0.31.3-SNAPSHOT"
    implementation("com.google.accompanist:accompanist-themeadapter-material3:$accompanist_version")
    implementation("com.google.accompanist:accompanist-webview:$accompanist_version")
    implementation("com.google.android.material:material:1.9.0")

    val splitties = "3.0.0"
    implementation("com.louiscad.splitties:splitties-appctx:$splitties")
    implementation("com.louiscad.splitties:splitties-systemservices:$splitties")
    implementation("com.louiscad.splitties:splitties-preferences:$splitties")
    implementation("com.louiscad.splitties:splitties-arch-room:$splitties")

    // https://square.github.io/okhttp/changelogs/changelog/
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.0.0-alpha.11"))
    implementation("com.squareup.okhttp3:okhttp-android")
    implementation("com.squareup.okhttp3:okhttp-coroutines")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps")

    implementation("com.squareup.okio:okio-jvm:3.3.0")

    implementation("com.mikepenz:aboutlibraries-core:10.7.0")

    implementation("dev.chrisbanes.insetter:insetter:0.6.1") // Dead Dependency
    implementation("dev.rikka.rikkax.core:core-ktx:1.4.1")
    implementation("dev.rikka.rikkax.insets:insets:1.3.0")
    implementation("dev.rikka.rikkax.layoutinflater:layoutinflater:1.3.0")

    implementation(platform("io.arrow-kt:arrow-stack:1.2.0-RC"))
    implementation("io.arrow-kt:arrow-fx-coroutines")

    // https://coil-kt.github.io/coil/changelog/
    implementation(platform("io.coil-kt:coil-bom:2.4.0"))
    implementation("io.coil-kt:coil-compose")
    implementation("io.coil-kt:coil-gif")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.5.1")
    implementation("org.jsoup:jsoup:1.16.1")

    val chunker = "3.5.2"
    debugImplementation("com.github.chuckerteam.chucker:library:$chunker")
    releaseImplementation("com.github.chuckerteam.chucker:library-no-op:$chunker")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

tasks.withType<KspTaskJvm>().configureEach {
    compilerOptions.jvmTarget.set(JVM_17)
}

aboutLibraries {
    duplicationMode = MERGE
    duplicationRule = GROUP
}

cargo {
    module = "src/main/rust"
    libname = "ehviewer_rust"
    targets = if (isRelease) listOf("arm", "x86", "arm64", "x86_64") else listOf("arm64")
    if (isRelease) profile = "release"
}

tasks.whenObjectAdded {
    if ((name == "mergeDebugJniLibFolders" || name == "mergeReleaseJniLibFolders")) {
        dependsOn("cargoBuild")
        // fix mergeDebugJniLibFolders  UP-TO-DATE
        inputs.dir(buildDir.resolve("rustJniLibs/android"))
    }
}
