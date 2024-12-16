package io.github.seggan.kmixin.gen

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.metadata.KmClassifier
import kotlin.metadata.isInline
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.jvm.signature

class WrapperGenerator(delegate: ClassVisitor, private val generator: JavaGenerator) :
    ClassVisitor(Opcodes.ASM9, delegate) {

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String,
        interfaces: Array<out String>
    ) {
        if (Opcodes.ACC_RECORD and access != 0) {
            throw MixinGenerationException("Records are not supported")
        }
        super.visit(version, access, generator.mixinName, signature, superName, interfaces)
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        if (descriptor != "Lorg/spongepowered/asm/mixin/Mixin;") {
            return null
        }
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitInnerClass(name: String, outerName: String, innerName: String, access: Int) {
        // do nothing
    }

    override fun visitNestHost(nestHost: String) {
        // do nothing
    }

    override fun visitOuterClass(owner: String, name: String, descriptor: String) {
        // do nothing
    }

    override fun visitTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean
    ): AnnotationVisitor? {
        return null
    }

    override fun visitAttribute(attribute: Attribute) {
        // do nothing
    }

    override fun visitNestMember(nestMember: String) {
        // do nothing
    }

    override fun visitRecordComponent(
        name: String,
        descriptor: String?,
        signature: String?
    ): RecordComponentVisitor? {
        return null
    }

    override fun visitPermittedSubclass(permittedSubclass: String) {
        // do nothing
    }

    override fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        value: Any?
    ): FieldVisitor? {
        return null
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        val annotations = generator.annotations[name + descriptor] ?: return null
        if (!annotations.any { it.startsWith("Lorg/spongepowered") }) return null
        if (Opcodes.ACC_STATIC and access == 0) {
            throw MixinGenerationException("Non-static methods are not supported")
        }
        if (Opcodes.ACC_PRIVATE and access == 0) {
            throw MixinGenerationException("All methods in a mixin must be private")
        }

        val type = Type.getMethodType(descriptor)
        var returnType = type.returnType
        val argumentTypes = type.argumentTypes.toMutableList()
        val isInject = if (annotations.any { it == Descriptors.SPONGE_INJECT }) {
            if (type.returnType == Type.VOID_TYPE && !argumentTypes.any { it.descriptor == Descriptors.SPONGE_CALLBACK_INFO }) {
                argumentTypes.add(Type.getType(CallbackInfo::class.java))
            } else if (!argumentTypes.any { it.descriptor == Descriptors.SPONGE_CALLBACK_INFO_RETURNABLE }) {
                argumentTypes.removeLast()
                argumentTypes.add(Type.getType(CallbackInfoReturnable::class.java))
                returnType = Type.VOID_TYPE
            }
            true
        } else {
            false
        }

        val receiverType: String?
        var newAccess = access
        if (generator.metadata is KotlinClassMetadata.FileFacade) {
            val pkg = generator.metadata.kmPackage
            val func = pkg.functions.first { it.name == name && it.signature?.descriptor == descriptor }
            if (func.receiverParameterType != null) {
                val classifier = func.receiverParameterType!!.classifier
                if (classifier !is KmClassifier.Class) {
                    throw MixinGenerationException("Unsupported receiver type: $classifier")
                }
                receiverType = classifier.name
                newAccess = newAccess and Opcodes.ACC_STATIC.inv()
                argumentTypes.removeAt(0)
            } else {
                receiverType = null
            }

            if (func.isInline) {
                // Not my fault if your function explodes
                return super.visitMethod(
                    access,
                    name,
                    type.descriptor,
                    signature,
                    exceptions
                )
            }
        } else {
            receiverType = null
        }

        val newType = Type.getMethodType(returnType, *argumentTypes.toTypedArray())
        return WrappedMethodGenerator(
            super.visitMethod(
                newAccess,
                name,
                newType.descriptor,
                signature,
                exceptions
            ),
            name,
            type,
            receiverType,
            isInject
        )
    }

    private inner class WrappedMethodGenerator(
        private val delegate: MethodVisitor,
        private val name: String,
        private val type: Type,
        private val castType: String?,
        private val isInject: Boolean
    ) : MethodVisitor(Opcodes.ASM9) {

        private val isCir = type.returnType != Type.VOID_TYPE && isInject

        override fun visitCode() {
            delegate.visitCode()
            var i = 0
            if (castType != null) {
                delegate.visitVarInsn(Opcodes.ALOAD, 0)
                delegate.visitTypeInsn(Opcodes.CHECKCAST, castType)
                i++
            }
            val cirIndex = i
            val ret = type.returnType
            if (isCir) {
                if (i < type.argumentTypes.size) {
                    delegate.visitVarInsn(Opcodes.ALOAD, i)
                    val name = if (ret.sort == Type.OBJECT || ret.sort == Type.ARRAY) {
                        "getReturnValue"
                    } else {
                        "getReturnValue${ret.descriptor}"
                    }
                    val descriptor = if (ret.sort == Type.OBJECT || ret.sort == Type.ARRAY) {
                        "()Ljava/lang/Object;"
                    } else {
                        "()" + ret.descriptor
                    }
                    delegate.visitMethodInsn(
                        INVOKEVIRTUAL,
                        callbackInfoReturnableName,
                        name,
                        descriptor,
                        false
                    )
                    if (ret.sort == Type.OBJECT || ret.sort == Type.ARRAY) {
                        delegate.visitTypeInsn(Opcodes.CHECKCAST, ret.internalName)
                    }
                }
                i++
            }
            while (i < type.argumentTypes.size) {
                delegate.visitVarInsn(type.argumentTypes[i].getOpcode(Opcodes.ILOAD), i)
                i++
            }
            delegate.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                generator.implName,
                name,
                type.descriptor,
                false
            )
            if (isCir) {
                delegate.box(ret)
                delegate.visitVarInsn(Opcodes.ALOAD, cirIndex)
                delegate.visitInsn(Opcodes.SWAP)
                delegate.visitMethodInsn(
                    INVOKEVIRTUAL,
                    callbackInfoReturnableName,
                    "setReturnValue",
                    "(Ljava/lang/Object;)V",
                    false
                )
            }
            if (isInject) {
                delegate.visitInsn(Opcodes.RETURN)
            } else {
                delegate.visitInsn(type.returnType.getOpcode(Opcodes.IRETURN))
            }
            delegate.visitEnd()
        }

        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
            return delegate.visitAnnotation(descriptor, visible)
        }
    }
}

private val Type.boxed: Type?
    get() = when (sort) {
        Type.BOOLEAN -> Type.getType(Boolean::class.java)
        Type.BYTE -> Type.getType(Byte::class.java)
        Type.SHORT -> Type.getType(Short::class.java)
        Type.CHAR -> Type.getType(Character::class.java)
        Type.INT -> Type.getType(Integer::class.java)
        Type.LONG -> Type.getType(Long::class.java)
        Type.FLOAT -> Type.getType(Float::class.java)
        Type.DOUBLE -> Type.getType(Double::class.java)
        else -> null
    }

private fun MethodVisitor.box(type: Type) {
    val boxed = type.boxed ?: return
    visitMethodInsn(
        Opcodes.INVOKESTATIC,
        boxed.internalName,
        "valueOf",
        "(${type.descriptor})L${boxed.internalName};",
        false
    )
}

private val callbackInfoReturnableName = Type.getType(CallbackInfoReturnable::class.java).internalName