package io.github.seggan.kmixin

import io.github.seggan.kmixin.gen.JavaGenerator
import io.github.seggan.kmixin.gen.MixinGenerationException
import kotlinx.serialization.json.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class GenerationTask : DefaultTask() {

    private val buildDir = project.layout.buildDirectory.get().asFile

    @TaskAction
    fun generate() {
        val mixins = getMixins() ?: return
        for (config in mixins) {
            val subDir = if (config.global) "main" else config.environment.toString()
            val file = buildDir.resolve("resources/$subDir/${config.file}")
            val mixinJson = Json.parseToJsonElement(file.readText())
            val pkg = mixinJson.jsonObject["package"]?.jsonPrimitive?.content ?: continue
            val mixinList =
                mixinJson.jsonObject[config.environment.toString()]?.jsonArray?.map { it.jsonPrimitive.content }
                    ?: continue
            for (mixin in mixinList) {
                val packagePath = pkg.replace('.', '/')
                val mixinFile = buildDir.resolve("classes/kotlin/$subDir/$packagePath/$mixin.class")
                if (!mixinFile.exists()) continue
                val generator = try {
                    JavaGenerator(pkg, mixinFile)
                } catch (_: IllegalStateException) {
                    // No Kotlin metadata, skip
                    continue
                }
                try {
                    generator.doStuff()
                } catch (e: MixinGenerationException) {
                    throw MixinGenerationException("Failed to generate implementation for $mixin: ${e.message}")
                }
            }
        }
    }

    private fun getMixins(): List<MixinConfig>? {
        val fabricModJson = buildDir.resolve("resources/main/fabric.mod.json")
        if (!fabricModJson.exists()) return null
        val modConfig = Json.parseToJsonElement(fabricModJson.readText())
        val mixinConfigs = modConfig.jsonObject["mixins"]?.jsonArray ?: return null
        val mixins = mutableListOf<MixinConfig>()
        for (config in mixinConfigs) {
            if (config is JsonPrimitive) {
                val path = config.content
                for (env in MixinConfig.Environment.values()) {
                    mixins.add(MixinConfig(path, env, true))
                }
            } else {
                val path = config.jsonObject["config"]?.jsonPrimitive?.content ?: continue
                val envString = config.jsonObject["environment"]?.jsonPrimitive?.content ?: continue
                if (envString == "*") {
                    for (env in MixinConfig.Environment.values()) {
                        mixins.add(MixinConfig(path, env, true))
                    }
                } else {
                    mixins.add(MixinConfig(path, MixinConfig.Environment.valueOf(envString.uppercase()), false))
                }
            }
        }
        return mixins
    }
}

