package io.github.seggan.kmixin

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile

class KMixinPluginProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = object : SymbolProcessor {
        override fun process(resolver: Resolver): List<KSAnnotated> {
            val processor = KMixinProcessor(environment.codeGenerator, resolver)
            val mixins = resolver.getSymbolsWithAnnotation(SpongeNames.MIXIN)
            val skipped = mutableListOf<KSAnnotated>()
            for (mixin in mixins) {
                when (mixin) {
                    is KSFile -> processor.processFile(mixin)
                    is KSClassDeclaration -> skipped.add(mixin) // TODO: process class-level mixins
                    else -> skipped.add(mixin)
                }
            }
            return skipped
        }
    }
}