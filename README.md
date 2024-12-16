# KMixin

Write your mixins fearlessly in Kotlin! Mixins [famously don't support Kotlin](https://github.com/SpongePowered/Mixin/issues/245) 
This Gradle plugin automatically generates Java wrapper code that forwards calls to your Kotlin mixin,
so you don't have to worry about Kotlin's extra code generation. Check out the 
[wiki](https://github.com/Seggan/kmixin/wiki) for more information.

### This project is still early in development. While I encourage you to try it out, please report any issues you encounter.

## Currently unsupported features
- MixinExtras
- Interface injection
- `@Shadow`