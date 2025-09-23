# KMixin

Write your mixins fearlessly in Kotlin! Mixins [famously don't support Kotlin](https://github.com/SpongePowered/Mixin/issues/245). This is a [KSP](https://https://github.com/google/ksp) plugin that generates Java stubs around your Kotlin mixins to make them compatible with Mixin.

## Usage
Add the KSP plugin to your `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "2.2.10"
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
}

dependencies {
    ksp("io.github.seggan:kmixin:0.1.1")
}
```

And that's all for the build configuration! See the [wiki](htpps://github.com/Seggan/kmixin/wiki) for code examples and more detailed usage instructions.

## Unimplemented features
- Locals access
- `@Shadow` will simply not work
- I have not tested this with MixinExtras, so it may or may not work