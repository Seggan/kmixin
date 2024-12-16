package io.github.seggan.kmixin.gen

import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

object Descriptors {
    val KOTLIN_METADATA: String = Metadata::class.java.descriptorString()
    val SPONGE_MIXIN: String = Mixin::class.java.descriptorString()
    val SPONGE_INJECT: String = Inject::class.java.descriptorString()
    val SPONGE_CALLBACK_INFO: String = CallbackInfo::class.java.descriptorString()
    val SPONGE_CALLBACK_INFO_RETURNABLE: String = CallbackInfoReturnable::class.java.descriptorString()
}