package io.github.seggan.kmixin

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class KMixinPluginProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = KMixinProcessor(environment.codeGenerator)
}