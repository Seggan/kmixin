package io.github.seggan.kmixin

data class MixinConfig(val file: String, val environment: Environment, val global: Boolean) {
    enum class Environment {
        CLIENT,
        SERVER;

        override fun toString(): String {
            return name.lowercase()
        }
    }
}