package io.github.seggan.kmixin.gen

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.ASM9
import java.io.File
import kotlin.metadata.ClassKind
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.kind

class JavaGenerator(private val pkg: String, private val file: File) {

    private val reader = ClassReader(file.readBytes())
    private val slashPkg = pkg.replace('.', '/')

    val mixinName = "$slashPkg/${file.nameWithoutExtension}"
    val implName = "$slashPkg-impl/${file.nameWithoutExtension}"

    val annotations: Map<String, List<String>>
    val metadata: KotlinClassMetadata

    init {
        val visitor = AnnotationFinder()
        reader.accept(visitor, 0)
        metadata = visitor.metadata ?: error("No Kotlin metadata found in $file")
        annotations = visitor.annotations
    }

    private val isInterface = metadata is KotlinClassMetadata.Class && metadata.kmClass.kind == ClassKind.INTERFACE

    private class AnnotationFinder : ClassVisitor(ASM9) {

        var foundMixin = false
        var metadata: KotlinClassMetadata? = null

        val annotations = mutableMapOf<String, MutableList<String>>()

        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            if (descriptor == Descriptors.KOTLIN_METADATA) {
                return MetadataReader(::metadata)
            }
            return null
        }

        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            return object : MethodVisitor(ASM9) {
                override fun visitAnnotation(aDescriptor: String, visible: Boolean): AnnotationVisitor? {
                    annotations.computeIfAbsent(name + descriptor) { mutableListOf() }.add(aDescriptor)
                    return null
                }
            }
        }
    }

    private inner class ImplEditor(delegate: ClassVisitor) : ClassVisitor(ASM9, delegate) {
        private val replace: (String) -> Boolean = if (isInterface) {
            { it == Descriptors.KOTLIN_METADATA }
        } else {
            { it.startsWith("Lorg/spongepowered") }
        }

        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String,
            interfaces: Array<out String>?
        ) {
            super.visit(version, access, if (isInterface) mixinName else implName, signature, superName, interfaces)
        }

        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            if (replace(descriptor)) {
                return null
            }
            return super.visitAnnotation(descriptor, visible)
        }

        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            val superVisitor = super.visitMethod(
                access and Opcodes.ACC_PRIVATE.inv() or Opcodes.ACC_PUBLIC,
                name,
                descriptor,
                signature,
                exceptions
            )
            return object : MethodVisitor(ASM9, superVisitor) {
                override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
                    if (replace(descriptor)) {
                        return null
                    }
                    return super.visitAnnotation(descriptor, visible)
                }

                override fun visitFieldInsn(
                    opcode: Int,
                    owner: String,
                    name: String,
                    descriptor: String?
                ) {
                    super.visitFieldInsn(opcode, if (owner == mixinName) implName else owner, name, descriptor)
                }

                override fun visitMethodInsn(
                    opcode: Int,
                    owner: String,
                    name: String,
                    descriptor: String?,
                    isInterface: Boolean
                ) {
                    super.visitMethodInsn(opcode, if (owner == mixinName) implName else owner, name, descriptor, isInterface)
                }
            }
        }
    }

    fun doStuff() {
        if (!isInterface) {
            val writer = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
            val visitor = WrapperGenerator(writer, this)
            reader.accept(visitor, 0)
            file.writeBytes(writer.toByteArray())
        }
        removeOldAnnotations()
    }

    private fun removeOldAnnotations() {
        val genDir =
            if (isInterface) file.parentFile else file.parentFile.resolveSibling("${pkg.substringAfterLast('.')}-impl")
        genDir.mkdirs()
        val writer = ClassWriter(reader, 0)
        val visitor = ImplEditor(writer)
        reader.accept(visitor, 0)
        genDir.resolve(file.name).writeBytes(writer.toByteArray())
    }
}