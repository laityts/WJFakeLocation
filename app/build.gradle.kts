plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.room)
    id("jacoco") // 代码覆盖率
}

android {
    namespace = "com.steadywj.wjfakelocation"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.steadywj.wjfakelocation"
        minSdk = 33
        targetSdk = 35
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = System.getenv("VERSION_NAME") ?: "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // CI/CD 签名配置
            signingConfig = if (System.getenv("KEYSTORE_PATH") != null) {
                signingConfigs.create("release") {
                    storeFile = file(System.getenv("KEYSTORE_PATH"))
                    storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD") 
                        ?: project.findProperty("RELEASE_KEYSTORE_PASSWORD") as String? ?: ""
                    keyAlias = System.getenv("RELEASE_KEY_ALIAS") 
                        ?: project.findProperty("RELEASE_KEY_ALIAS") as String? ?: ""
                    keyPassword = System.getenv("RELEASE_KEY_PASSWORD") 
                        ?: project.findProperty("RELEASE_KEY_PASSWORD") as String? ?: ""
                }
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    
    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)
    
    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    
    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)
    
    // AMap (高德地图)
    implementation(libs.amap.search)
    implementation(libs.amap.location)
    implementation(libs.amap.map)
    
    // Baidu Map (百度地图)
    implementation("com.baidu.lbsyun:BaiduMapSDK_Map:7.5.4")
    implementation("com.baidu.lbsyun:BaiduMapSDK_Search:7.5.4")
    implementation("com.baidu.lbsyun:BaiduMapSDK_Location:7.5.4")
    
    // Supabase (云同步后端)
    implementation(platform("io.github.jan-tennert.supabase:bom:2.0.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    
    // Lua 脚本引擎
    implementation("org.luaj:luaj-jse:3.0.1")
    
    // Xposed
    compileOnly(libs.xposed.api)
    
    // Utilities
    implementation(libs.hiddenapibypass)
    implementation(libs.security.crypto)
    
    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// ==================== Jacoco 代码覆盖率配置 ====================
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.test)
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    
    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*Hilt*.*",
        "**/*_Factory.*",
        "**/*_MembersInjector.*"
    )
    
    val kotlinTree = fileTree(mapOf(
        "dir" to "$buildDir/tmp/kotlin-classes/debug",
        "excludes" to fileFilter
    ))
    
    val javaTree = fileTree(mapOf(
        "dir" to "$buildDir/intermediates/javac/debug",
        "excludes" to fileFilter
    ))
    
    classDirectories.setFrom(files(kotlinTree, javaTree))
    sourceDirectories.setFrom(files("$projectDir/src/main/java"))
    executionData.setFrom(fileTree(mapOf(
        "dir" to "$buildDir",
        "includes" to listOf("outputs/unit_test_code_coverage/**/**/*.exec")
    )))
}