package io.github.seggan.kmixin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import org.spongepowered.asm.mixin.Mixin

class KMixinProcessor(private val generator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val mixins = resolver.getSymbolsWithAnnotation(Mixin::class.qualifiedName!!)
        println(mixins)
        return emptyList()
    }
}