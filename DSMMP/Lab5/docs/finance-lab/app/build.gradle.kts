plugins { alias(libs.plugins.android.application) }
android { namespace = "com.example.finance"; compileSdk = 35
 defaultConfig { applicationId = "com.example.finance"; minSdk = 24; targetSdk = 35 }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies { implementation(libs.room.runtime); annotationProcessor(libs.room.compiler); implementation(libs.lifecycle.viewmodel); implementation(libs.lifecycle.livedata); implementation(libs.work); implementation(libs.core); implementation("androidx.activity:activity:1.9.3"); testImplementation(libs.junit) }
