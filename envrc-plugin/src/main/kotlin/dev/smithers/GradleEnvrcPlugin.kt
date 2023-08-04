package dev.smithers

import java.io.File
import java.util.concurrent.TimeUnit

// 👇 this value can be imported by users in kotlin scripts
val envrc = ScriptApi()

class GradleEnvrcPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(project: org.gradle.api.Project) {
        // 👇 this value can be referenced by users in groovy scripts
        project.extensions.extraProperties["envrc"] = envrc
    }
}

class ScriptApi {
    private val envrcFile = EnvrcFile()
    operator fun get(key: String): String? {
        return envrcFile.extract(key)
    }
}

private class EnvrcFile {
    private var _envrcFile: File? = null

    private fun envrc(): File {
        val result = _envrcFile ?: findEnvrc()!!
        _envrcFile = result
        return result
    }

    fun extract(variableName: String): String? {
        val dollar = "\$"
        val script = """
                source .envrc &> /dev/null
                if [[ -z "${dollar}{${variableName}+z}" ]]; then
                    echo -n $SPECIAL_NULL_VALUE
                else
                    echo -n ${dollar}{${variableName}}
                fi
                """
        val process = ProcessBuilder()
            .command("bash", "-c", script)
            .directory(envrc().parentFile)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        process.waitFor(60, TimeUnit.SECONDS)
        val result = process.inputStream.bufferedReader().readText()
        if (result == SPECIAL_NULL_VALUE) {
            return null
        }
        return result
    }
}

private fun findEnvrc(dir: File = File(System.getProperty("user.dir"))): File? {
    if (!dir.isDirectory) {
        throw IllegalStateException("not a directory: $dir")
    }
    val envrc = File("${dir.absolutePath}/.envrc")
    if (envrc.exists()) {
        println("Reading .envrc at ${envrc.absolutePath}")
        return envrc
    }
    if (dir.absolutePath == "/") {
        throw IllegalStateException("Unable to find .envrc")
    }
    return findEnvrc((dir.absoluteFile.parentFile))
}

private const val SPECIAL_NULL_VALUE = "special-null-value"
