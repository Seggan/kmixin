package io.github.seggan.kmixin

import org.gradle.api.Plugin
import org.gradle.api.Project

class KMixinPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val allDependTasks = listOf(
            "compileKotlin",
            "compileClientKotlin",
            "compileServerKotlin",
            "processResources",
            "processClientResources",
            "processServerResources"
        )
        val thisTask = project.tasks.register("generateJavaMixinWrappers", GenerationTask::class.java)
        /// whyy do you have to be like this
        project.afterEvaluate {
            thisTask.configure {
                for (task in allDependTasks) {
                    dependsOn(project.tasks.findByName(task) ?: continue)
                }
            }

            listOf("jar", "clientClasses", "serverClasses").mapNotNull(project.tasks::findByName).forEach {
                it.dependsOn(thisTask)
            }
        }
    }
}