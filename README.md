# Envrc Gradle Plugin

This plugin enables gradle scripts to read environment variables directly from
a `.envrc` rather than relying on [direnv](https://direnv.net/) to populate
them in a containing shell.

I made this because IntelliJ bypasses direnv when you run gradle tasks within it.

# Example Usage

- **Kotlin**

  ```kotlin
  import dev.smithers.envrc

  plugins {
      id("dev.smithers.envrc")
  }

  val value = System.getenv("ENVIRONMENT_VARIABLE") ?: envrc["ENVIRONMENT_VARIABLE"]
  ```

- **Groovy**

  ```groovy
  plugins {
      id("dev.smithers.envrc")
  }

  value = System.getenv("ENVIRONMENT_VARIABLE") ?: envrc.ENVIRONMENT_VARIABLE
  ```
