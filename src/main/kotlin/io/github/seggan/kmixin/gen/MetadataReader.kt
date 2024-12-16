package io.github.seggan.kmixin.gen

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.jvm.Metadata
import kotlin.reflect.KMutableProperty
import kotlin.reflect.jvm.isAccessible

class MetadataReader(private val result: KMutableProperty<KotlinClassMetadata?>) : AnnotationVisitor(Opcodes.ASM9) {

    private val metaValues = mutableMapOf<String, Any>()

    override fun visit(name: String, value: Any) {
        when (name) {
            "k" -> metaValues["kind"] = value as Int
            "mv" -> metaValues["metadataVersion"] = value as IntArray
            "xs" -> metaValues["extraString"] = value as String
            "pn" -> metaValues["packageName"] = value as String
            "xi" -> metaValues["extraInt"] = value as Int
        }
    }

    override fun visitArray(name: String): AnnotationVisitor? {
        return when (name) {
            "d1" -> ArrayCollector("data1")
            "d2" -> ArrayCollector("data2")
            else -> null
        }
    }

    private inner class ArrayCollector(private val name: String) : AnnotationVisitor(Opcodes.ASM9) {
        private val values = mutableListOf<String>()

        override fun visit(name: String?, value: Any) {
            values.add(value as String)
        }

        override fun visitEnd() {
            metaValues[name] = values.toTypedArray()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun visitEnd() {
        val metadata = Metadata(
            kind = metaValues["kind"] as? Int,
            metadataVersion = metaValues["metadataVersion"] as? IntArray,
            data1 = metaValues["data1"] as? Array<String>,
            data2 = metaValues["data2"] as? Array<String>,
            extraString = metaValues["extraString"] as? String,
            packageName = metaValues["packageName"] as? String,
            extraInt = metaValues["extraInt"] as? Int,
        )
        result.isAccessible = true
        result.setter.call(KotlinClassMetadata.readStrict(metadata))
    }
}