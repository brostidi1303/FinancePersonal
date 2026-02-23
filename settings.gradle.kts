pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://jitpack.io")

        maven {
            url = uri("https://cboost.jfrog.io/artifactory/chartboost-ads/")
        }
        maven {    url =uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea") }
        maven(url = "https://sdk.tapjoy.com/")
        maven(url = "https://maven.google.com")
        maven(url = "https://android-sdk.is.com")
        maven(url = "https://repo1.maven.org/maven2/")
        maven(url = "https://artifacts.applovin.com/android")
        maven(url = "https://artifact.bytedance.com/repository/pangle")
        maven(url = "https://cboost.jfrog.io/artifactory/chartboost-ads/")
        maven(url = "https://repo.premiumads.net/artifactory/mobile-ads-sdk/")
        maven(url = "https://cboost.jfrog.io/artifactory/chartboost-mediation")
        maven(url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")


        //// Yandex ADS SDK
        maven { url = uri("https://android-sdk.is.com/") } // IronSource
        maven { url = uri("https://artifact.bytedance.com/repository/pangle") } // Pangle
        maven { url = uri("https://sdk.tapjoy.com/") } // Tapjoy
        maven {
            url =
                uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        }
        // Mintegral
        maven { url = uri("https://cboost.jfrog.io/artifactory/chartboost-ads/") } // Chartboost
        maven { url = uri("https://dl.appnext.com/") } // AppNext
        //Non-listed GP market applications， Android X Version
        maven {
            url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_china")
        }
        //Launch GP market application， Android X Version
        maven {
            url =
                uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        }
        //Non-listed GP market applications, not the Android X version
        maven {
            url =
                uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_support/")
        }
    }
}

rootProject.name = "FinanceApp"
include(":app")
 