import com.google.samples.apps.nowinandroid.task.GitChangedFilesValueSource
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

class DetektConventionPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = target.run {
        val baseConfigsPath =
            "$rootDir/build-logic/convention/src/main/kotlin/com/google/samples/apps/nowinandroid/detekt"
        val resourceFiles = "**/resources/**"
        val buildFiles = "**/build/**"
        val libs = the<LibrariesForLibs>()
        pluginManager.apply(libs.plugins.detekt.get().pluginId)

        extensions.configure<DetektExtension> {
            parallel = true
            buildUponDefaultConfig = true
            ignoreFailures = false
            autoCorrect = false
            source.setFrom(layout.projectDirectory)
            config.setFrom(files("$baseConfigsPath/detekt.yml"))
            baseline = file("$baseConfigsPath/baseline.yml")
            ignoredBuildTypes = listOf("release")
            ignoredFlavors = listOf("local", "production")
        }

        dependencies {
            detektPlugins(libs.detekt.formatting.get())
        }

        tasks.withType<Detekt>().configureEach {
            parallel = true
            buildUponDefaultConfig = true
            ignoreFailures = false
            autoCorrect = false
            config.setFrom(files("$baseConfigsPath/detekt.yml"))
            baseline.set(file("$baseConfigsPath/baseline.yml"))
            setSource(file(layout.projectDirectory))
            exclude(resourceFiles, buildFiles)
            reports {
                xml.required.set(false)
                md.required.set(false)
            }
        }
        tasks.withType<DetektCreateBaselineTask>().configureEach {
            exclude(resourceFiles, buildFiles)
        }
        val changedFiles = providers.of(GitChangedFilesValueSource::class) {}
        tasks.register<Detekt>("fastDetekt") {
            include(changedFiles.get())
            doLast {
                println("Called from fastDetekt! List of changed files: $changedFiles")
            }
        }
    }

    private fun DependencyHandler.detektPlugins(dependencyNotation: Any) {
        add("detektPlugins", dependencyNotation)
    }
}
