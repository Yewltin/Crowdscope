// settings.gradle.kts

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

println("⛓️ Mapbox token loaded: " +
        providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").orNull)
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        println("🔍 about to add Mapbox repo")
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication { create<BasicAuthentication>("basic") }
            credentials {
                username = "mapbox"
                password = providers
                .gradleProperty("MAPBOX_DOWNLOADS_TOKEN")
                    .orNull
                    ?.takeIf { it.isNotBlank() }
                    ?: error(
                        "⛔ Missing or empty MAPBOX_DOWNLOADS_TOKEN in <project>/gradle.properties"
                    )
            }
        }
    }
}

rootProject.name = "CrowdScopeAndroid"
include(":app")
